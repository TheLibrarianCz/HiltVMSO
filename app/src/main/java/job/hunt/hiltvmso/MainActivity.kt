package job.hunt.hiltvmso

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Header
import javax.inject.Inject
import javax.inject.Singleton

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val delegateVm: DishesViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colors.background
            ) {
                Column {
                    Greeting(mainViewModel = hiltViewModel())
                    Greeting(mainViewModel = viewModel())
                    Greeting(mainViewModel = delegateVm)
                }
            }
        }
    }
}

@Composable
fun Greeting(mainViewModel: DishesViewModel) {
    println(mainViewModel)

    mainViewModel.getDishes("f")
    val dishes by mainViewModel.dishes.observeAsState()
    Text(
        text = "Hello ${dishes}!"
    )
}

class DishList : ArrayList<String>()

data class DishListItem(
    val activeCount: Int,
    val actualPrice: Int,
    val description: String,
    val dishId: Int,
    val isLobobox: Int,
    val name: String,
    val orders: List<Order>,
    val originalPrice: Int,
    val photo: String,
    val tags: List<String>
)

data class Order(
    val actualPrice: Int,
    val customerId: String,
    val orderId: Int,
    val originalPrice: Int,
    val status: String
)

interface GetDishList {

    @GET(APIConstants.DISH_LIST)
    suspend fun getDishes(@Header("Authorization") authHeader: String): DishList

}

object APIConstants {
    const val BASE_URL = "http://my_server_link"
    const val DISH_LIST = "list_dishes"
}

class DishRepo @Inject constructor(
    private val dishList: GetDishList
) {
    suspend fun getDishList(authHeader: String): DishList {
        return DishList().apply { addAll(listOf("Item 1", "Item 2")) }
    }
}

@Module
@InstallIn(SingletonComponent::class)
object DishAPIModule {

    @Provides
    @Singleton
    fun provideGetDishList(retrofit: Retrofit): GetDishList {
        return retrofit.create(GetDishList::class.java)
    }

    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(APIConstants.BASE_URL)
            .build()
    }

}

@HiltViewModel
class DishesViewModel @Inject constructor(
    private val dishRepo: DishRepo
) : ViewModel() {

    private val _dishes = MutableLiveData<DishList>()
    val dishes: LiveData<DishList> = _dishes

    fun getDishes(authHeader: String) {
        viewModelScope.launch {
            val dishList = dishRepo.getDishList(authHeader)
            _dishes.postValue(dishList)
        }
    }
}