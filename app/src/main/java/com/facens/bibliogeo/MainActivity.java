package com.facens.bibliogeo;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.facens.bibliogeo.databinding.ActivityMainBinding;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_LOCALIZACAO = 10;

    private ActivityMainBinding binding;
    private ArrayAdapter<String> adapter;
    private final List<Livro> livrosEncontrados = new ArrayList<>();
    private final List<String> textosLivros = new ArrayList<>();
    private Livro livroSelecionado;
    private double latitudeAtual;
    private double longitudeAtual;
    private boolean temLocalizacao = false;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firestore = FirebaseFirestore.getInstance();
        configurarSpinners();
        configurarLista();

        binding.buttonBuscar.setOnClickListener(v -> buscarLivros());
        binding.buttonSalvar.setOnClickListener(v -> salvarLivro());
        binding.buttonVerSalvos.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, ListaLivrosActivity.class))
        );
    }

    private void configurarSpinners() {
        String[] situacoes = {"Biblioteca", "Livraria", "Aula", "Indicacao", "Leitura em casa", "Viagem", "Outro"};
        String[] status = {"Quero ler", "Lendo", "Concluido"};

        ArrayAdapter<String> adapterSituacao = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                situacoes
        );
        binding.spinnerSituacao.setAdapter(adapterSituacao);

        ArrayAdapter<String> adapterStatus = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                status
        );
        binding.spinnerStatus.setAdapter(adapterStatus);
    }

    private void configurarLista() {
        adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                textosLivros
        );
        binding.listLivrosApi.setAdapter(adapter);

        binding.listLivrosApi.setOnItemClickListener((parent, view, position, id) -> {
            livroSelecionado = livrosEncontrados.get(position);
            binding.textLivroSelecionado.setText("Selecionado: " + livroSelecionado.getTitulo());
            capturarLocalizacao();
        });
    }

    private void buscarLivros() {
        String termo = binding.editBusca.getText().toString().trim();

        if (termo.isEmpty()) {
            binding.editBusca.setError("Digite algo para pesquisar");
            return;
        }

        binding.textStatus.setText("Buscando livros...");
        livrosEncontrados.clear();
        textosLivros.clear();
        adapter.notifyDataSetChanged();

        new BuscarLivrosTask().execute(termo);
    }

    private void capturarLocalizacao() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCALIZACAO);
            return;
        }

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (locationManager == null) {
            binding.textCoordenadas.setText("Nao foi possivel acessar o GPS.");
            return;
        }

        try {
            Location ultimaLocalizacao = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (ultimaLocalizacao == null) {
                ultimaLocalizacao = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }

            if (ultimaLocalizacao != null) {
                atualizarCoordenadas(ultimaLocalizacao);
            } else {
                binding.textCoordenadas.setText("Capturando localizacao...");
                locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, new LocationListener() {
                    @Override
                    public void onLocationChanged(@NonNull Location location) {
                        atualizarCoordenadas(location);
                    }
                }, null);
            }
        } catch (SecurityException e) {
            binding.textCoordenadas.setText("Permissao de localizacao nao concedida.");
        } catch (Exception e) {
            binding.textCoordenadas.setText("Nao foi possivel capturar a localizacao.");
        }
    }

    private void atualizarCoordenadas(Location location) {
        latitudeAtual = location.getLatitude();
        longitudeAtual = location.getLongitude();
        temLocalizacao = true;
        binding.textCoordenadas.setText("Coordenadas: " + latitudeAtual + ", " + longitudeAtual);
    }

    private void salvarLivro() {
        if (livroSelecionado == null) {
            Toast.makeText(this, "Selecione um livro da lista", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!temLocalizacao) {
            Toast.makeText(this, "A localizacao ainda nao foi capturada", Toast.LENGTH_SHORT).show();
            capturarLocalizacao();
            return;
        }

        String situacao = binding.spinnerSituacao.getSelectedItem().toString();
        String status = binding.spinnerStatus.getSelectedItem().toString();
        String observacao = binding.editObservacao.getText().toString().trim();

        Map<String, Object> dados = new HashMap<>();
        dados.put("titulo", livroSelecionado.getTitulo());
        dados.put("autores", livroSelecionado.getAutores());
        dados.put("dataPublicacao", livroSelecionado.getDataPublicacao());
        dados.put("editora", livroSelecionado.getEditora());
        dados.put("situacao", situacao);
        dados.put("statusLeitura", status);
        dados.put("observacao", observacao);
        dados.put("latitude", latitudeAtual);
        dados.put("longitude", longitudeAtual);

        binding.buttonSalvar.setEnabled(false);
        firestore.collection("livros")
                .add(dados)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Livro salvo no Firebase", Toast.LENGTH_SHORT).show();
                    binding.editObservacao.setText("");
                    binding.buttonSalvar.setEnabled(true);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erro ao salvar no Firebase", Toast.LENGTH_SHORT).show();
                    binding.buttonSalvar.setEnabled(true);
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_LOCALIZACAO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                capturarLocalizacao();
            } else {
                binding.textCoordenadas.setText("Permissao de localizacao negada.");
                Toast.makeText(this, "Sem permissao nao da para salvar coordenadas", Toast.LENGTH_LONG).show();
            }
        }
    }

    private class BuscarLivrosTask extends AsyncTask<String, Void, List<Livro>> {
        private String erro = "";

        @Override
        protected List<Livro> doInBackground(String... params) {
            List<Livro> resultado = new ArrayList<>();

            try {
                String termo = URLEncoder.encode(params[0], "UTF-8");
                URL url = new URL("https://openlibrary.org/search.json?q=" + termo + "&limit=10");
                HttpURLConnection conexao = (HttpURLConnection) url.openConnection();
                conexao.setRequestMethod("GET");
                conexao.setConnectTimeout(10000);
                conexao.setReadTimeout(10000);

                int resposta = conexao.getResponseCode();
                if (resposta != HttpURLConnection.HTTP_OK) {
                    erro = "Erro na API: " + resposta;
                    return resultado;
                }

                BufferedReader leitor = new BufferedReader(new InputStreamReader(conexao.getInputStream()));
                StringBuilder json = new StringBuilder();
                String linha;
                while ((linha = leitor.readLine()) != null) {
                    json.append(linha);
                }
                leitor.close();

                JSONObject objeto = new JSONObject(json.toString());
                JSONArray docs = objeto.optJSONArray("docs");
                if (docs == null) {
                    return resultado;
                }

                for (int i = 0; i < docs.length(); i++) {
                    JSONObject item = docs.getJSONObject(i);

                    String titulo = item.optString("title", "Sem titulo");
                    String data = item.has("first_publish_year")
                            ? String.valueOf(item.optInt("first_publish_year"))
                            : "Nao informado";
                    String editora = lerPrimeiroValor(item.optJSONArray("publisher"));
                    String autores = lerAutores(item.optJSONArray("author_name"));

                    resultado.add(new Livro(titulo, autores, data, editora));
                }
            } catch (Exception e) {
                erro = "Falha ao buscar. Verifique a internet.";
            }

            return resultado;
        }

        @Override
        protected void onPostExecute(List<Livro> resultado) {
            livrosEncontrados.clear();
            textosLivros.clear();

            livrosEncontrados.addAll(resultado);
            for (Livro livro : resultado) {
                textosLivros.add(livro.textoParaLista());
            }
            adapter.notifyDataSetChanged();

            if (!erro.isEmpty()) {
                binding.textStatus.setText(erro);
            } else if (resultado.isEmpty()) {
                binding.textStatus.setText("Nenhum livro encontrado.");
            } else {
                binding.textStatus.setText("Resultados encontrados: " + resultado.size());
            }
        }

        private String lerAutores(JSONArray autoresJson) {
            if (autoresJson == null || autoresJson.length() == 0) {
                return "Nao informado";
            }

            StringBuilder autores = new StringBuilder();
            for (int i = 0; i < autoresJson.length(); i++) {
                if (i > 0) {
                    autores.append(", ");
                }
                autores.append(autoresJson.optString(i));
            }
            return autores.toString();
        }

        private String lerPrimeiroValor(JSONArray jsonArray) {
            if (jsonArray == null || jsonArray.length() == 0) {
                return "Nao informado";
            }

            return jsonArray.optString(0, "Nao informado");
        }
    }
}
