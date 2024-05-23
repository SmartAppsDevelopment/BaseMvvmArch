package com.example.technical.challenge.data.base

import android.app.Application
import android.content.Context
import android.view.View
import com.example.technical.challenge.R
import com.example.technical.challenge.utils.hasInternetConnection
import com.squareup.moshi.Moshi
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import retrofit2.http.HTTP
import java.io.IOException
import javax.inject.Inject

class SafeApiCaller2 @Inject constructor(@ApplicationContext private val context: Context, private val moshi: Moshi) {

//    suspend fun <T> safeApiCall(
//        dispatcher: CoroutineDispatcher,
//        apiCall: suspend () -> T
//    ): ResultWrapper2<T> {
//        return withContext(dispatcher) {
//            try {
//                ResultWrapper2.Success(apiCall.invoke())
////                val application = getApplication<Application>()
////                if(!hasInternetConnection(application)) {
////                    errorFieldVisibility.set(View.VISIBLE)
////                    errorFieldString.set(application.getString(R.string.error_no_internet))
////                    return
////                }
//
//            } catch (throwable: Throwable) {
//                when (throwable) {
//                    is IOException -> ResultWrapper2.Errors.NetworkError
//                    is HttpException -> {
//                        val code = throwable.code()
//                        val errorResponse = convertErrorBody(throwable)
//                        ResultWrapper2.Errors.GenericError(code, errorResponse)
//                    }
//                    else -> {
//                        ///ResultWrapper.GenericError(null, null)
//                        if(hasInternetConnection(context)){
//                            ResultWrapper2.Errors.InternetConnectionError
//                        }else{
//                            ResultWrapper2.Errors.InternetConnectionError
//                        }
//                    }
//                }
//            }
//        }
//    }


    suspend fun <T> safeApiCall(
        dispatcher: CoroutineDispatcher,
        apiCall: suspend () -> T
    ): ResultWrapper2<T> {
        return withContext(dispatcher) {
            try {
                ResultWrapper2.Success(apiCall.invoke())
            } catch (throwable: Throwable) {
                when (throwable) {
                    is IOException -> ResultWrapper2.ERROR(Errors.NetworkError)

                    is HttpException -> {
                        val code = throwable.code()
                        val errorResponse = convertErrorBody(throwable)

                        ResultWrapper2.ERROR(Errors.GenericError(code, errorResponse))
                    }

                    else -> {
                        if(hasInternetConnection(context)){
                            ResultWrapper2.ERROR(Errors.InternetConnectionError)
                        }else{
                            ResultWrapper2.ERROR(Errors.NotSure)
                        }
                    }
                }
            }
        }
    }

    private fun convertErrorBody(throwable: HttpException): ErrorResponse? {
        return try {
            throwable.response()?.errorBody()?.source()?.let {
                val moshiAdapter = moshi.adapter(ErrorResponse::class.java)
                moshiAdapter.fromJson(it)
            }
        } catch (exception: Exception) {
            null
        }
    }
}