package com.facens.bibliogeo;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.facens.bibliogeo.databinding.ActivityListaLivrosBinding;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class ListaLivrosActivity extends AppCompatActivity {

    private ActivityListaLivrosBinding binding;
    private FirebaseFirestore firestore;
    private ArrayAdapter<String> adapter;
    private final List<LivroCadastrado> livros = new ArrayList<>();
    private final List<String> textosLivros = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityListaLivrosBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firestore = FirebaseFirestore.getInstance();

        adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                textosLivros
        );
        binding.listLivrosSalvos.setAdapter(adapter);

        binding.buttonVoltar.setOnClickListener(v -> finish());

        binding.listLivrosSalvos.setOnItemClickListener((parent, view, position, id) ->
                abrirDialogEdicao(livros.get(position))
        );

        binding.listLivrosSalvos.setOnItemLongClickListener((parent, view, position, id) -> {
            confirmarExclusao(livros.get(position));
            return true;
        });

        carregarLivros();
    }

    @Override
    protected void onResume() {
        super.onResume();
        carregarLivros();
    }

    private void carregarLivros() {
        binding.textInfoLista.setText("Carregando livros...");
        livros.clear();
        textosLivros.clear();
        adapter.notifyDataSetChanged();

        firestore.collection("livros")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    livros.clear();
                    textosLivros.clear();

                    for (DocumentSnapshot documento : queryDocumentSnapshots.getDocuments()) {
                        LivroCadastrado livro = documento.toObject(LivroCadastrado.class);
                        if (livro != null) {
                            livro.setId(documento.getId());
                            livros.add(livro);
                            textosLivros.add(livro.textoParaLista());
                        }
                    }

                    adapter.notifyDataSetChanged();

                    if (livros.isEmpty()) {
                        binding.textInfoLista.setText("Nenhum livro salvo ainda.");
                    } else {
                        binding.textInfoLista.setText("Toque para editar. Segure para excluir. Total: " + livros.size());
                    }
                })
                .addOnFailureListener(e ->
                        binding.textInfoLista.setText("Erro ao carregar dados do Firebase.")
                );
    }

    private void abrirDialogEdicao(LivroCadastrado livro) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(32, 16, 32, 0);

        Spinner spinnerStatus = new Spinner(this);
        String[] status = {"Quero ler", "Lendo", "Concluido"};
        ArrayAdapter<String> adapterStatus = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                status
        );
        spinnerStatus.setAdapter(adapterStatus);
        for (int i = 0; i < status.length; i++) {
            if (status[i].equals(livro.getStatusLeitura())) {
                spinnerStatus.setSelection(i);
                break;
            }
        }

        EditText editObservacao = new EditText(this);
        editObservacao.setHint("Observacao");
        editObservacao.setMinLines(2);
        editObservacao.setText(livro.getObservacao());

        layout.addView(spinnerStatus);
        layout.addView(editObservacao);

        new AlertDialog.Builder(this)
                .setTitle(livro.getTitulo())
                .setMessage("Autor: " + livro.getAutores()
                        + "\nEditora: " + livro.getEditora()
                        + "\nPublicacao: " + livro.getDataPublicacao()
                        + "\nLocal: " + livro.getSituacao()
                        + "\nCoordenadas: " + livro.getLatitude() + ", " + livro.getLongitude())
                .setView(layout)
                .setPositiveButton("Salvar", (dialog, which) -> {
                    String novoStatus = spinnerStatus.getSelectedItem().toString();
                    String novaObservacao = editObservacao.getText().toString().trim();

                    firestore.collection("livros")
                            .document(livro.getId())
                            .update("statusLeitura", novoStatus, "observacao", novaObservacao)
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(this, "Livro atualizado no Firebase", Toast.LENGTH_SHORT).show();
                                carregarLivros();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Erro ao atualizar livro", Toast.LENGTH_SHORT).show()
                            );
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void confirmarExclusao(LivroCadastrado livro) {
        new AlertDialog.Builder(this)
                .setTitle("Excluir livro")
                .setMessage("Deseja excluir \"" + livro.getTitulo() + "\"?")
                .setPositiveButton("Excluir", (dialog, which) -> {
                    firestore.collection("livros")
                            .document(livro.getId())
                            .delete()
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(this, "Livro excluido do Firebase", Toast.LENGTH_SHORT).show();
                                carregarLivros();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Erro ao excluir livro", Toast.LENGTH_SHORT).show()
                            );
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}
