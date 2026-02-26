package com.hanin.smartcareer.network;

import com.hanin.smartcareer.models.OpenAIRequest;
import com.hanin.smartcareer.models.OpenAIResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

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
