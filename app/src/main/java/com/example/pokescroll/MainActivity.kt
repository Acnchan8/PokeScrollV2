package com.example.pokescroll

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.codepath.asynchttpclient.AsyncHttpClient
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler
import okhttp3.Headers

class MainActivity : AppCompatActivity() {
    private lateinit var pokemonList: MutableList<Pokemon>
    private lateinit var rvPokemons: RecyclerView
    private val numberOfPokemonToFetch = 30
    private var totalPokemonCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        pokemonList = mutableListOf()
        rvPokemons = findViewById(R.id.pokemon_list)
        rvPokemons.layoutManager = LinearLayoutManager(this@MainActivity)
        rvPokemons.adapter = PokemonAdapter(pokemonList)

        val button = findViewById<Button>(R.id.pokemonButton)
        val pokeballImage  = findViewById<ImageView>(R.id.pokeballImage)

        button.setOnClickListener {
            pokeballImage.visibility = View.GONE
            button.visibility = View.GONE
            rvPokemons.visibility = View.VISIBLE
            fetchTotalPokemonCount()
        }
    }

    private fun fetchTotalPokemonCount() {
        val client = AsyncHttpClient()
        val url = "https://pokeapi.co/api/v2/pokemon-species/"

        client.get(url, object : JsonHttpResponseHandler() {
            override fun onSuccess(statusCode: Int, headers: Headers, json: JsonHttpResponseHandler.JSON) {
                Log.d("Pokemon", "Total count fetch successful")
                totalPokemonCount = json.jsonObject.getInt("count")
                getRandomPokemons()
            }

            override fun onFailure(statusCode: Int, headers: Headers?, errorResponse: String, throwable: Throwable?) {
                Log.d("PokemonAPI Error", errorResponse)
            }
        })
    }

    private fun getRandomPokemons() {
        val client = AsyncHttpClient()
        val url = "https://pokeapi.co/api/v2/pokemon?limit=${numberOfPokemonToFetch}"

        client[url, object : JsonHttpResponseHandler() {
            override fun onSuccess(statusCode: Int, headers: Headers, json: JsonHttpResponseHandler.JSON) {
                Log.d("Pokemon", "response successful")
                for (i in 1..numberOfPokemonToFetch) {
                    val randomId = (1..totalPokemonCount).random()
                    val pokemonUrl = "https://pokeapi.co/api/v2/pokemon/$randomId"
                    getPokemonDetails(pokemonUrl)
                }
            }
            override fun onFailure(statusCode: Int, headers: Headers?, errorResponse: String, throwable: Throwable?) {
                Log.d("PokemonAPI Error", errorResponse)
            }
        }]
    }

    private fun getPokemonDetails(pokemonUrl: String) {
        val client = AsyncHttpClient()

        client[pokemonUrl, object : JsonHttpResponseHandler() {
            override fun onSuccess(statusCode: Int, headers: Headers, json: JsonHttpResponseHandler.JSON) {
                Log.d("Pokemon", "Details fetch successful")
                var name = json.jsonObject.getString("name")
                name = name.replaceFirstChar {if (it.isLowerCase()) it.uppercase() else it.toString() }
                val sprites = json.jsonObject.getJSONObject("sprites")
                val imageUrl = sprites.optString("front_default", "")

                val typesArray = json.jsonObject.getJSONArray("types")
                val typesList = mutableListOf<String>()
                for (i in 0 until typesArray.length()) {
                    val typeEntry = typesArray.getJSONObject(i).getJSONObject("type")
                    val typeName = typeEntry.getString("name")
                    typesList.add(typeName.replaceFirstChar { if (it.isLowerCase()) it.uppercase() else it.toString() })
                }
                val type = "Types: ${typesList.joinToString(separator = ", ")}"
                val newPokemon = Pokemon(name, imageUrl, type)
                runOnUiThread {
                    pokemonList.add(newPokemon)
                    rvPokemons.adapter?.notifyDataSetChanged()
                }
            }
            override fun onFailure(statusCode: Int, headers: Headers?, errorResponse: String, throwable: Throwable?) {
                Log.d("PokemonAPI Error", errorResponse)
            }
        }]
    }
}