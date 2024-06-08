package com.example.notetakingapp4;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.*;

import java.io.IOException;

public class OpenAIChat {

    String result;

    public OpenAIChat(String content) throws IOException, JSONException {
        OkHttpClient client = new OkHttpClient();
        String url = "https://api.openai-hk.com/v1/chat/completions";
        String str = "My note content is '" + content +
                "', Here are Six Categories :{Personal, Work, Meeting, Travel, Life, Other}, Please choose the most qualified category. Notice: you should only reply one word included in the six categories and Don't reply any other words";

        MediaType mediaType = MediaType.parse("application/json");
        JSONObject jsonBody = new JSONObject();
        jsonBody.put("max_tokens", 1200);
        jsonBody.put("model", "gpt-3.5-turbo");
        jsonBody.put("temperature", 0.8);
        jsonBody.put("top_p", 1);
        jsonBody.put("presence_penalty", 1);
        JSONArray messages = new JSONArray();
        JSONObject message = new JSONObject();
        message.put("role", "user");
        message.put("content", str);  // 确保content已正确处理避免JSON错误
        messages.put(message);
        jsonBody.put("messages", messages);

        RequestBody body = RequestBody.create(mediaType, jsonBody.toString());
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer hk-7aebjj10000344396db053b772ca3e6fbb389bf4549746ae")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }
            String jsonResponse = response.body().string();
            JSONObject obj = new JSONObject(jsonResponse);
            result = obj.getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    String getResult(){
        return result;
    }


}