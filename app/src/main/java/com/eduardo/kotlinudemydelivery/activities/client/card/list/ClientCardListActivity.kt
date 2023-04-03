package com.eduardo.kotlinudemydelivery.activities.client.card.list

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.eduardo.kotlinudemydelivery.Providers.CardsProvider
import com.eduardo.kotlinudemydelivery.R
import com.eduardo.kotlinudemydelivery.adapters.CardAdapter
import com.eduardo.kotlinudemydelivery.databinding.ActivityClientCardListBinding
import com.eduardo.kotlinudemydelivery.models.Cards
import com.eduardo.kotlinudemydelivery.models.ResponseHttp
import com.eduardo.kotlinudemydelivery.models.User
import com.eduardo.kotlinudemydelivery.utils.SharedPref
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ClientCardListActivity : AppCompatActivity() {

    val TAG = "ClienteCard"
    private lateinit var binding: ActivityClientCardListBinding
    var toolbar: Toolbar? = null
    var edittextNumberCard: EditText? = null
    var edittextExpiration: EditText? = null
    var cardProvider: CardsProvider?=null
    var user: User? = null
    var sharedPref: SharedPref? = null

    var gson = Gson()
    var height = 0
    var trescuartos = 0
    var dpheight = 0f
    var heightResulta = 0f

    var adapter: CardAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClientCardListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPref = SharedPref(this)
        getUserFromSession()
        cardProvider = CardsProvider(user?.sessionToken!!)
        binding.recyclerviewCard.layoutManager = LinearLayoutManager(this)
        edittextNumberCard = findViewById(R.id.edittext_number_card)
        edittextExpiration = findViewById(R.id.edittext_expiration)
        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metrics)
        val width = metrics. widthPixels// ancho absoluto en pixels
        height = metrics.heightPixels // alto absoluto en pixels
        trescuartos = (height/10)
        dpheight = 370 * getResources().getDisplayMetrics().density; // convertir pixeles a dp

        toolbar = findViewById(R.id.toolbar)
        toolbar?.setTitleTextColor(ContextCompat.getColor(this,R.color.black))
        toolbar?.title = "Mis Metodos de Pago"
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.viewTransparent.visibility = View.INVISIBLE
//        val params = binding.linear.layoutParams
//        val height2 = binding.linear.height

        heightResulta =  - dpheight

//        binding.fabCardCreate.setOnClickListener { goToCardCreate() }
        binding.linear.translationY =  heightResulta
        binding.abrirFormCard.setOnClickListener {
            binding.viewTransparent.visibility = View.VISIBLE
            animateLayout(heightResulta, trescuartos.toFloat(),1.0f)
        }
        binding.btnCerrarlayout.setOnClickListener {
            binding.viewTransparent.visibility = View.INVISIBLE
            animateLayout(trescuartos.toFloat(),heightResulta,0.0f)
        }
        validNumberCard()
        validExpirationCard()
//        edittextNumberCard?.setOnClickListener {  binding.linear.translationY =  trescuartos.toFloat()}
        binding.btnCreateCard.setOnClickListener { createCard() }

        getCardsByUser(user?.id!!)
    }

    private fun animateLayout(y: Float, destino: Float, alfa: Float) {
        binding.linear.translationY =y
        binding.linear.animate()
            .translationY(destino)
            .setInterpolator(LinearInterpolator())
            .setDuration(300)
            .alpha(alfa)
            .setStartDelay(200)
            .start()
    }

    private fun validNumberCard(){
        edittextNumberCard?.addTextChangedListener(object : TextWatcher {
            private val space = ' '
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {

                if (s?.length == 1){
                    val primero = s.get(0)
//                    Log.d(TAG,"primer digito:  ${primero}")
                    if (primero == '5'){
                        binding.imageViewIconCard.setImageResource(R.drawable.mastercard)
                    } else if(primero == '4'){
                        binding.imageViewIconCard.setImageResource(R.drawable.visa)
                    } else if(primero == '3'){
                        binding.imageViewIconCard.setImageResource(R.drawable.american_express)
                    }
                } else if(s?.length == 0){
                    binding.imageViewIconCard.setImageResource(R.drawable.tarjeta)
                }
                var pos = 0;
                while (true){
                    if (pos >= s?.length!!) break
                    if (space == s[pos] && (((pos +1)%5)!= 0 || pos +1 == s.length)){
                        s.delete(pos,pos+1)
                    }else{
                        pos++
                    }
                }

                pos = 4;
                while (true){
                    if (pos >= s.length)break
                    val c = s[pos]

                    if ("0123456789".indexOf(c) >= 0){
                        s.insert(pos,"" + space)
                    }
                    pos +=5
                }
            }

        })
    }

    private fun validExpirationCard(){
        edittextExpiration?.addTextChangedListener(object : TextWatcher {
            private val space = ' '
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s?.length == 2) {
                    if(start==2 && before==1 && !s.toString().contains("/")){
                        edittextExpiration?.setText(""+s.toString().get(0));
                        edittextExpiration?.setSelection(1);
                    }
                    else {
                        edittextExpiration?.setText("$s/");
                        edittextExpiration?.setSelection(3);
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }

        })
    }

    private fun createCard(){
        val numberCard = edittextNumberCard?.text.toString()
        val expiration = edittextExpiration?.text.toString()
        val name = binding.edittextPropietarioCard?.text.toString()
        val cvv = binding.edittextCvv?.text.toString()
//        Log.d(TAG, "NUMBER CARD $numberCard")
        if (isValidForm(numberCard,expiration,name,cvv)){
            val card = Cards(
                id_client =  user?.id,
                number_card =  numberCard,
                expiration = expiration,
                name_client = name,
                cvv =cvv
            )
            cardProvider?.create(card)?.enqueue(object : Callback<ResponseHttp>{
                override fun onResponse(
                    call: Call<ResponseHttp>,
                    response: Response<ResponseHttp>
                ) {
                    if (response.body()?.isSuccess == true){
                        Toast.makeText(this@ClientCardListActivity,response.body()?.message,Toast.LENGTH_LONG).show()
                        animateLayout(trescuartos.toFloat(),heightResulta,0.0f)
                        binding.viewTransparent.visibility = View.INVISIBLE
                        clearFormCard()
                        getCardsByUser(user?.id!!)
                    }
                }

                override fun onFailure(call: Call<ResponseHttp>, t: Throwable) {
                    Toast.makeText(this@ClientCardListActivity, "Se produjo un error ${t.message}", Toast.LENGTH_LONG).show()
                    Log.d(TAG,"ERROR: ${t.message}")
               }

            })
        }
    }

    private fun clearFormCard(){
        edittextNumberCard?.setText("")
        edittextExpiration?.setText("")
        binding.edittextPropietarioCard.setText("")
        binding.edittextCvv.setText("")
    }

    private fun getUserFromSession(){
        val gson = Gson()
        if (!sharedPref?.getData("user").isNullOrBlank()){
            //si el usuario exite en sesion
            user = gson.fromJson(sharedPref?.getData("user"), User::class.java)
//            Log.e(TAG, "Usuario: $user")
        }
    }

    private fun isValidForm(
        numberCard: String,
        expiration: String,
        name_client: String,
        cvv: String
    ):Boolean{

        if (numberCard.isNullOrBlank()){
            Toast.makeText(this, "Ingrese el numero de su tarjeta", Toast.LENGTH_LONG).show()
            return false
        }

        if (expiration.isNullOrBlank()){
            Toast.makeText(this, "Ingrese la expiración de su tarjeta", Toast.LENGTH_LONG).show()
            return false
        }

        if (name_client.isNullOrBlank()){
            Toast.makeText(this, "Ingrese el propietario de la tarjeta", Toast.LENGTH_LONG).show()
            return false
        }

        if (cvv.isNullOrBlank()){
            Toast.makeText(this, "Ingrese el cvv de la tarjeta", Toast.LENGTH_LONG).show()
            return false
        }

        return true
    }

    fun getCardsByUser(idClient: String){
        cardProvider?.getCards(idClient)?.enqueue(object : Callback<ArrayList<Cards>>{
            override fun onResponse(
                call: Call<ArrayList<Cards>>,
                response: Response<ArrayList<Cards>>
            ) {
                if (response.body() != null){
                    val cards = response.body()!!
                    val efectivo = Cards(
                        number_card = "Efectivo"
                    )
                    cards.add(efectivo)
//                    Log.d(TAG,"Error: ${cards}")
                    adapter = CardAdapter(this@ClientCardListActivity,cards)
                    binding.recyclerviewCard.adapter = adapter
                }
            }

            override fun onFailure(call: Call<ArrayList<Cards>>, t: Throwable) {
                Log.d(TAG,"Error: ${t.message}")
            }

        })
    }

}