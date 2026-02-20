package com.hanin.smartcareer.network;

import com.hanin.smartcareer.BuildConfig;

import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Singleton Retrofit client configured for the OpenAI API.
 *
 * <h3>Key features</h3>
 * <ul>
 *   <li>Reads the API key from {@code BuildConfig.OPENAI_API_KEY}
 *       (injected at build-time from {@code local.properties}).</li>
 *   <li>Adds an {@code Authorization: Bearer <key>} header to every request
 *       via an OkHttp {@link Interceptor}.</li>
 *   <li>60-second connect / read / write timeouts.</li>
 *   <li>{@code HttpLoggingInterceptor} at {@code BODY} level in debug builds.</li>
 * </ul>
 *
 * <h3>Security note</h3>
 * Embedding the API key in the APK is acceptable <strong>for testing only</strong>.
 * In production, all OpenAI requests must be proxied through your own
 * backend server so the key is never shipped to end-user devices.
 */
public final class RetrofitClient {

    // Groq API base URL (OpenAI-compatible, free tier)
    private static final String BASE_URL = "https://api.groq.com/openai/";

    // Singleton instance
    private static Retrofit retrofit = null;

    private RetrofitClient() { /* prevent instantiation */ }

    /**
     * Returns (and lazily creates) the singleton {@link Retrofit} instance.
     */
    public static Retrofit getInstance() {
        if (retrofit == null) {
            synchronized (RetrofitClient.class) {
                if (retrofit == null) {

                    // ── Auth interceptor ─────────────────────────────
                    // Injects the Bearer token and required OpenRouter headers.
                    Interceptor authInterceptor = chain -> {
                        Request original = chain.request();
                        Request authorised = original.newBuilder()
                                .header("Authorization",
                                        "Bearer " + BuildConfig.OPENAI_API_KEY)
                                .header("Content-Type", "application/json")
                                .header("HTTP-Referer", "https://smartcareer.app")
                                .header("X-Title", "SmartCareer")
                                .build();
                        return chain.proceed(authorised);
                    };

                    // ── Logging interceptor (debug builds only) ──────
                    HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
                    logging.setLevel(BuildConfig.DEBUG
                            ? HttpLoggingInterceptor.Level.BODY
                            : HttpLoggingInterceptor.Level.NONE);

                    // ── OkHttp client ────────────────────────────────
                    OkHttpClient client = new OkHttpClient.Builder()
                            .addInterceptor(authInterceptor)
                            .addInterceptor(logging)
                            .connectTimeout(60, TimeUnit.SECONDS)
                            .readTimeout(60, TimeUnit.SECONDS)
                            .writeTimeout(60, TimeUnit.SECONDS)
                            .build();

                    // ── Retrofit builder ─────────────────────────────
                    retrofit = new Retrofit.Builder()
                            .baseUrl(BASE_URL)
                            .client(client)
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();
                }
            }
        }
        return retrofit;
    }

    /**
     * Convenience helper – returns an implementation of the given
     * service interface backed by the singleton Retrofit client.
     */
    public static <T> T createService(Class<T> serviceClass) {
        return getInstance().create(serviceClass);
    }
}
