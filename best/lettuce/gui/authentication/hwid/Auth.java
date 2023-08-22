package best.lettuce.gui.authentication.hwid;

import java.io.*;
import java.util.Objects;


public class Auth {
    public static boolean loginuuid(String uuid, String hwid) {
//        String responsestr;
//        OkHttpClient client = new OkHttpClient().newBuilder()
//                .build();
//        RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
//                .addFormDataPart("actions","auth")
//                .addFormDataPart("hwid", hwid)
//                .addFormDataPart("uuid", uuid)
//                .build();
//        Request request = new Request.Builder()
//                .url("https://lettuce.gay/useractions.php")
//                .method("POST", body)
//                .build();
//        try {
//            Response response = client.newCall(request).execute();
//            responsestr = Objects.requireNonNull(response.body()).string();
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//
//        return responsestr.equals("200");
        return true;
    }
}
