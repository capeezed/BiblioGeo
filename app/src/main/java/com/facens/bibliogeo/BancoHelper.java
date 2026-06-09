package com.facens.bibliogeo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class BancoHelper extends SQLiteOpenHelper {
    private static final String NOME_BANCO = "bibliogeo.db";
    private static final int VERSAO = 1;

    public BancoHelper(Context context) {
        super(context, NOME_BANCO, null, VERSAO);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE livros (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "titulo TEXT, " +
                "autores TEXT, " +
                "data_publicacao TEXT, " +
                "editora TEXT, " +
                "situacao TEXT, " +
                "status_leitura TEXT, " +
                "observacao TEXT, " +
                "latitude REAL, " +
                "longitude REAL)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS livros");
        onCreate(db);
    }

    public long inserirLivro(Livro livro, String situacao, String status, String observacao,
                             double latitude, double longitude) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues valores = new ContentValues();
        valores.put("titulo", livro.getTitulo());
        valores.put("autores", livro.getAutores());
        valores.put("data_publicacao", livro.getDataPublicacao());
        valores.put("editora", livro.getEditora());
        valores.put("situacao", situacao);
        valores.put("status_leitura", status);
        valores.put("observacao", observacao);
        valores.put("latitude", latitude);
        valores.put("longitude", longitude);

        return db.insert("livros", null, valores);
    }

    public List<LivroCadastrado> listarLivros() {
        List<LivroCadastrado> livros = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM livros ORDER BY id DESC", null);

        while (cursor.moveToNext()) {
            livros.add(new LivroCadastrado(
                    String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow("id"))),
                    cursor.getString(cursor.getColumnIndexOrThrow("titulo")),
                    cursor.getString(cursor.getColumnIndexOrThrow("autores")),
                    cursor.getString(cursor.getColumnIndexOrThrow("data_publicacao")),
                    cursor.getString(cursor.getColumnIndexOrThrow("editora")),
                    cursor.getString(cursor.getColumnIndexOrThrow("situacao")),
                    cursor.getString(cursor.getColumnIndexOrThrow("status_leitura")),
                    cursor.getString(cursor.getColumnIndexOrThrow("observacao")),
                    cursor.getDouble(cursor.getColumnIndexOrThrow("latitude")),
                    cursor.getDouble(cursor.getColumnIndexOrThrow("longitude"))
            ));
        }

        cursor.close();
        return livros;
    }

    public int atualizarLivro(int id, String status, String observacao) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues valores = new ContentValues();
        valores.put("status_leitura", status);
        valores.put("observacao", observacao);

        return db.update("livros", valores, "id = ?", new String[]{String.valueOf(id)});
    }

    public int excluirLivro(int id) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete("livros", "id = ?", new String[]{String.valueOf(id)});
    }
}
