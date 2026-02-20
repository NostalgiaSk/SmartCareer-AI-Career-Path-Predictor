package com.hanin.smartcareer.network;

import com.hanin.smartcareer.models.OpenAIRequest;
import com.hanin.smartcareer.models.OpenAIResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Retrofit service interface for the OpenAI Chat Completion API.
 *
 * <p>Only one endpoint is needed — the chat completions route.
 * The {@code Authorization} header is injected automatically by the
 * OkHttp interceptor configured in {@link RetrofitClient}.
 */
public interface OpenAIService {

    /**
     * Sends a chat completion request to OpenAI.
     *
     * @param request The structured request body containing the model,
     *                messages, temperature, and max tokens.
     * @return A {@link Call} wrapping the API response.
     */
    @POST("v1/chat/completions")
    Call<OpenAIResponse> getChatCompletion(@Body OpenAIRequest request);
}
