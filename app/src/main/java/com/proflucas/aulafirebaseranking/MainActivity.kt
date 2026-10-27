package com.proflucas.aulafirebaseranking

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.ComponentActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : ComponentActivity() {

    private var pontos = 0

    private val pontuacoesRef = FirebaseDatabase
        .getInstance()
        .getReference("pontuacoes")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val campoNome = findViewById<EditText>(R.id.campoNome)
        val textoPontuacao = findViewById<TextView>(R.id.textoPontuacao)
        val textoRanking = findViewById<TextView>(R.id.textoRanking)
        val botaoAumentarPontos = findViewById<Button>(R.id.botaoAumentarPontos)
        val botaoSalvar = findViewById<Button>(R.id.botaoSalvar)

        botaoAumentarPontos.setOnClickListener {
            pontos++
            textoPontuacao.text = "Pontuação: $pontos"
        }

        botaoSalvar.setOnClickListener {
            val nome = campoNome.text.toString().trim()

            if (nome.isEmpty()) {
                campoNome.error = "Digite seu nome"
            } else {
                salvarPontuacao(nome)
            }
        }

        carregarRanking(textoRanking)
    }

    private fun salvarPontuacao(nome: String) {
        val pontuacao = Pontuacao(nome, pontos)

        pontuacoesRef.push()
            .setValue(pontuacao)
            .addOnSuccessListener {
                // Pontuação salva.
            }
            .addOnFailureListener { erro ->
                textoErro(erro.message)
            }
    }

    private fun carregarRanking(textoRanking: TextView) {
        pontuacoesRef
            .orderByChild("pontos")
            .limitToLast(10)
            .addValueEventListener(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    val pontuacoes = mutableListOf<Pontuacao>()

                    for (item in snapshot.children) {
                        item.getValue(Pontuacao::class.java)?.let {
                            pontuacoes.add(it)
                        }
                    }

                    pontuacoes.reverse()

                    val linhas = pontuacoes.mapIndexed { indice, item ->
                        "${indice + 1}. ${item.nome} - ${item.pontos} pontos"
                    }

                    textoRanking.text = if (linhas.isEmpty()) {
                        "Ainda não há pontuações."
                    } else {
                        "Ranking:\n" + linhas.joinToString("\n")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    textoRanking.text = "Erro ao carregar o ranking."
                }
            })
    }

    private fun textoErro(mensagem: String?) {
        android.util.Log.e("Firebase", mensagem ?: "Falha ao salvar pontuação")
    }
}