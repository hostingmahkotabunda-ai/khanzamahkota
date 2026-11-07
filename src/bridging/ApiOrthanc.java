package bridging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fungsi.koneksiDB;
import fungsi.sekuel;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.swing.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.sql.Connection;
import java.util.Collections;
//import org.apache.http.HttpResponse;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import java.nio.file.*;
import java.util.*;
import java.io.*;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

public class ApiOrthanc {
    private HttpHeaders headers;
    private Connection koneksi=koneksiDB.condb();
    private sekuel Sequel=new sekuel();
    private JsonNode root;
    private SimpleDateFormat tanggalNow = new SimpleDateFormat("yyyy-MM-dd");
    private SimpleDateFormat jamNow = new SimpleDateFormat("HH:mm:ss");
    private HttpEntity<String> requestEntity;
    private ObjectMapper mapper = new ObjectMapper();
    private SSLContext sslContext;
    private SSLSocketFactory sslFactory;
    private Scheme scheme;
    private HttpComponentsClientHttpRequestFactory factory;
    private String auth, authEncrypt, requestJson;
    private byte[] encodedBytes;
    private int i = 1;

    public ApiOrthanc() {
        try {
            auth = koneksiDB.USERORTHANC() + ":" + koneksiDB.PASSORTHANC();
            encodedBytes = Base64.encodeBase64(auth.getBytes());
            authEncrypt = new String(encodedBytes);
        } catch (Exception ex) {
            System.out.println("Notifikasi : " + ex);
        }
    }

    public String Auth() {
        return authEncrypt;
    }
//        public String getTglPeriksa(String NoRawat) {
//            return Sequel.cariIsi("SELECT tgl_registrasi FROM reg_periksa WHERE no_rkm_medis=?", Norm);
//        }
//
//        public String getJam(String NoRawat) {
//            return Sequel.cariIsi("SELECT jam_reg FROM reg_periksa WHERE no_rkm_medis=?", NoRawat);
//        }
    public JsonNode AmbilSeries(String Norm, String Tanggal1, String Tanggal2) {
        System.out.println("Percobaan Mengambil Photo Pasien : " + Norm);
        
        try {
            headers = new HttpHeaders();
            System.out.println("Auth : " + authEncrypt);
            headers.add("Authorization", "Basic " + authEncrypt);
            requestJson = "{" +
                    "\"Level\": \"Study\"," +
                    "\"Expand\": true," +
                    "\"Query\": {" +
                    "\"StudyDate\": \"" + Tanggal1 + "-" + Tanggal2 + "\"," +
                    "\"PatientID\": \"" + Norm + "\"" +
                    "}" +
                    "}";
            System.out.println("Request JSON : " + requestJson);
            requestEntity = new HttpEntity<>(requestJson, headers);
            System.out.println("URL : " + koneksiDB.URLORTHANC() + ":" + koneksiDB.PORTORTHANC() + "/tools/find");
            requestJson = getRest().exchange(koneksiDB.URLORTHANC() + ":" + koneksiDB.PORTORTHANC() + "/tools/find", HttpMethod.POST, requestEntity, String.class).getBody();
            System.out.println("Result JSON : " + requestJson);
            root = mapper.readTree(requestJson);
        } catch (Exception e) {
            System.out.println("Notifikasi : " + e);
            JOptionPane.showMessageDialog(null, "Gagal mengambil data dari Orthanc, silahkan hubungi administrator ..!!");
        }
        return root;
    }

    public JsonNode AmbilPng(String NoRawat, String Series) {
        System.out.println("Percobaan Mengambil Gambar PNG : " + NoRawat + ", Series : " + Series);
        try {
            headers = new HttpHeaders();
            System.out.println("Auth : " + authEncrypt);
            headers.add("Authorization", "Basic " + authEncrypt);
            requestEntity = new HttpEntity(headers);
            System.out.println("URL : " + koneksiDB.URLORTHANC() + ":" + koneksiDB.PORTORTHANC() + "/series/" + Series);
            requestJson = getRest().exchange(koneksiDB.URLORTHANC() + ":" + koneksiDB.PORTORTHANC() + "/series/" + Series, HttpMethod.GET, requestEntity, String.class).getBody();
            System.out.println("Result JSON : " + requestJson);
            root = mapper.readTree(requestJson);
            i = 1;
            for (JsonNode list : root.path("Instances")) {
                System.out.println("Mengambil Gambar PNG " + koneksiDB.URLORTHANC() + ":" + koneksiDB.PORTORTHANC() + "/instances/" + list.asText() + "/preview");
                headers = new HttpHeaders();
                headers.add("Authorization", "Basic " + authEncrypt);
                headers.add("Accept", "image/png");
                headers.setAccept(Collections.singletonList(MediaType.APPLICATION_OCTET_STREAM));
                headers.setAccept(Collections.singletonList(MediaType.IMAGE_JPEG));
                HttpEntity<String> entity = new HttpEntity<>(headers);
                ResponseEntity<byte[]> response = getRest().exchange(koneksiDB.URLORTHANC() + ":" + koneksiDB.PORTORTHANC() + "/instances/" + list.asText() + "/preview", HttpMethod.GET, entity, byte[].class);
                Files.write(Paths.get("./gambarradiologi/" + NoRawat + i + ".png"), response.getBody());
                i++;
            }
            JOptionPane.showMessageDialog(null, "Pengambilan Gambar PNG dari Orthanc berhasil, silahkan lihat di dalam folder Aplikasi..!!");
        } catch (Exception e) {
            System.out.println("Notifikasi : " + e);
            JOptionPane.showMessageDialog(null, "Gagal mengambil Gambar PNG dari Orthanc, silahkan hubungi administrator ..!!");
        }
        return root;
        
        
    }
    

            public JsonNode AmbilJpg(String NoRawat, String Series) {
            System.out.println("Percobaan Mengambil Gambar JPG : " + NoRawat + ", Series : " + Series);
//            String tgl_periksa = getTglPeriksa(NoRawat);
//            String jam = getJam(NoRawat);
            try {
                
                headers = new HttpHeaders();
                System.out.println("Auth : " + authEncrypt);
                headers.add("Authorization", "Basic " + authEncrypt);
                requestEntity = new HttpEntity(headers);
                System.out.println("URL : " + koneksiDB.URLORTHANC() + ":" + koneksiDB.PORTORTHANC() + "/series/" + Series);
                requestJson = getRest().exchange(koneksiDB.URLORTHANC() + ":" + koneksiDB.PORTORTHANC() + "/series/" + Series, HttpMethod.GET, requestEntity, String.class).getBody();
                System.out.println("Result JSON : " + requestJson);
                root = mapper.readTree(requestJson);
                int i = 1;
                for (JsonNode list : root.path("Instances")) {
                    System.out.println("Mengambil Gambar JPG " + koneksiDB.URLORTHANC() + ":" + koneksiDB.PORTORTHANC() + "/instances/" + list.asText() + "/preview");
                    headers = new HttpHeaders();
                    headers.add("Authorization", "Basic " + authEncrypt);
                    headers.setAccept(Collections.singletonList(MediaType.IMAGE_JPEG));
                    HttpEntity<String> entity = new HttpEntity<>(headers);
                    ResponseEntity<byte[]> response = getRest().exchange(koneksiDB.URLORTHANC() + ":" + koneksiDB.PORTORTHANC() + "/instances/" + list.asText() + "/preview", HttpMethod.GET, entity, byte[].class);
                    //String docPath = "gambarradiologi";
                    String fileName = "./gambarradiologi/" + NoRawat + "_" + tanggalNow.format(new Date()) + "_" + jamNow.format(new Date()).replaceAll(":", "")+ "_" + i + ".jpg";
                    Files.write(Paths.get(fileName), response.getBody());

                    // Mengunggah gambar setelah menyimpannya
                    
                    uploadImage(fileName, "radiologi/pages/upload");

            i++;
        }
        JOptionPane.showMessageDialog(null, "Pengambilan Gambar JPG dari Orthanc berhasil, silahkan lihat di dalam folder Aplikasi..!!");
    } catch (Exception e) {
        System.out.println("Notifikasi : " + e);
        JOptionPane.showMessageDialog(null, "Gagal mengambil Gambar JPG dari Orthanc, silahkan hubungi administrator ..!!");
    }
    return root;
}
            private void uploadImage(String fileName, String docPath) {
    try {
        File file = new File(fileName);
        byte[] data = FileUtils.readFileToByteArray(file);
        HttpClient httpClient = new DefaultHttpClient();

        HttpPost postRequest = new HttpPost("http://" + koneksiDB.HOSTHYBRIDWEB() + ":" + koneksiDB.PORTWEB() + "/" + koneksiDB.HYBRIDWEB() + "/radiologi/upload.php?doc=" + docPath);

        MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
        ByteArrayBody fileBody = new ByteArrayBody(data, file.getName());
        reqEntity.addPart("file", fileBody);

        postRequest.setEntity(reqEntity);

        HttpResponse response = httpClient.execute(postRequest);
        String responseString = EntityUtils.toString(response.getEntity(), "UTF-8");
        System.out.println("Response Code: " + response.getStatusLine().getStatusCode());
        System.out.println("Response Body: " + responseString);

        if (response.getStatusLine().getStatusCode() == 200) {
            JSONObject jsonResponse = new JSONObject(responseString);
            String status = jsonResponse.getString("status");
            String message = jsonResponse.getString("message");

            if ("success".equals(status)) {
                String filePath = jsonResponse.getString("path");
                System.out.println("File successfully uploaded: " + fileName);
                System.out.println("File saved at: " + filePath);
            } else {
                System.out.println("Failed to upload file: " + fileName);
                System.out.println("Error: " + message);
            }
        } else {
            System.out.println("Failed to upload file: " + fileName);
            System.out.println("Response: " + responseString); // Log respons untuk debugging
        }
    } catch (Exception e) {
        System.out.println("Upload error: " + e);
    }
}

//           private void uploadImage(String fileName, String docPath) {
//                try {
//                    File file = new File(fileName);
//                    byte[] data = FileUtils.readFileToByteArray(file);
//                    HttpClient httpClient = new DefaultHttpClient();
//
//                    String url = docPath + "?doc=radiologi/pages/upload";
//                    HttpPost postRequest = new HttpPost(url);
//
//                    MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
//                    ByteArrayBody fileBody = new ByteArrayBody(data, fileName);
//                    reqEntity.addPart("file", fileBody);
//
//                    postRequest.setEntity(reqEntity);
//
//                    HttpResponse response = httpClient.execute(postRequest);
//                    String responseString = EntityUtils.toString(response.getEntity(), "UTF-8");
//
//                    System.out.println("Response: " + response.getStatusLine().getStatusCode());
//                    System.out.println("Response: " + response.getStatusLine().getStatusCode());
//                    System.out.println("Response body: " + responseString); // Tambahkan logging respons body
//
//                    if (response.getStatusLine().getStatusCode() == 200) {
//                        JSONObject jsonResponse = new JSONObject(responseString);
//                        String status = jsonResponse.getString("status");
//                        String message = jsonResponse.getString("message");
//
//                        if ("success".equals(status)) {
//                            String filePath = jsonResponse.getString("path");
//                            System.out.println("File successfully uploaded: " + fileName);
//                            System.out.println("File saved at: " + filePath);
//                        } else {
//                            System.out.println("Failed to upload file: " + fileName);
//                            System.out.println("Error: " + message);
//                        }
//                    } else {
//                        System.out.println("Failed to upload file: " + fileName);
//                        System.out.println("Response: " + responseString); // Log respons untuk debugging
//                    }
//
//                } catch (Exception e) {
//                    System.out.println("Upload error: " + e);
//                }
//}
        

//    public JsonNode AmbilJpg(String NoRawat, String Series) {
//        System.out.println("Percobaan Mengambil Gambar JPG : " + NoRawat + ", Series : " + Series);
//        try {
//            headers = new HttpHeaders();
//            System.out.println("Auth : " + authEncrypt);
//            headers.add("Authorization", "Basic " + authEncrypt);
//            requestEntity = new HttpEntity(headers);
//            System.out.println("URL : " + koneksiDB.URLORTHANC() + ":" + koneksiDB.PORTORTHANC() + "/series/" + Series);
//            requestJson = getRest().exchange(koneksiDB.URLORTHANC() + ":" + koneksiDB.PORTORTHANC() + "/series/" + Series, HttpMethod.GET, requestEntity, String.class).getBody();
//            System.out.println("Result JSON : " + requestJson);
//            root = mapper.readTree(requestJson);
//            int i = 1;
//            for (JsonNode list : root.path("Instances")) {
//                System.out.println("Mengambil Gambar JPG " + koneksiDB.URLORTHANC() + ":" + koneksiDB.PORTORTHANC() + "/instances/" + list.asText() + "/preview");
//                headers = new HttpHeaders();
//                headers.add("Authorization", "Basic " + authEncrypt);
//                headers.add("Accept", "image/jpeg");
//                headers.setAccept(Collections.singletonList(MediaType.APPLICATION_OCTET_STREAM));
//                headers.setAccept(Collections.singletonList(MediaType.IMAGE_JPEG));
//                HttpEntity<String> entity = new HttpEntity<>(headers);
//                ResponseEntity<byte[]> response = getRest().exchange(koneksiDB.URLORTHANC() + ":" + koneksiDB.PORTORTHANC() + "/instances/" + list.asText() + "/preview", HttpMethod.GET, entity, byte[].class);
//                String fileName = "./gambarradiologi/" + NoRawat + i + ".jpg";
//                Files.write(Paths.get(fileName), response.getBody());
//
//                // Mengunggah gambar setelah menyimpannya
////                String documentPath = "http://" + koneksiDB.HOSTHYBRIDWEB() + ":" + koneksiDB.PORTWEB() + "/" + koneksiDB.HYBRIDWEB() + "/radiologi/upload.php?doc=";
//                uploadImage(fileName, "http://"+koneksiDB.HOSTHYBRIDWEB()+":"+koneksiDB.PORTWEB()+"/"+koneksiDB.HYBRIDWEB()+"/radiologi/pages/upload");
//                //uploadImage(fileName, "http://"+koneksiDB.HOSTHYBRIDWEB()+":"+koneksiDB.PORTWEB()+"/"+koneksiDB.HYBRIDWEB()+"/radiologi/pages/upload/"+ NoRawat); // Sesuaikan path dokumen sesuai kebutuhan
//
//                i++;
//            }
//            JOptionPane.showMessageDialog(null, "Pengambilan Gambar JPG dari Orthanc berhasil, silahkan lihat di dalam folder Aplikasi..!!");
//        } catch (Exception e) {
//            System.out.println("Notifikasi : " + e);
//            JOptionPane.showMessageDialog(null, "Gagal mengambil Gambar JPG dari Orthanc, silahkan hubungi administrator ..!!");
//        }
//        return root;
//    }

    public JsonNode AmbilBmp(String NoRawat, String Series) {
        System.out.println("Percobaan Mengambil Gambar BMP : " + NoRawat + ", Series : " + Series);
        try {
            headers = new HttpHeaders();
            System.out.println("Auth : " + authEncrypt);
            headers.add("Authorization", "Basic " + authEncrypt);
            requestEntity = new HttpEntity(headers);
            System.out.println("URL : " + koneksiDB.URLORTHANC() + ":" + koneksiDB.PORTORTHANC() + "/series/" + Series);
            requestJson = getRest().exchange(koneksiDB.URLORTHANC() + ":" + koneksiDB.PORTORTHANC() + "/series/" + Series, HttpMethod.GET, requestEntity, String.class).getBody();
            System.out.println("Result JSON : " + requestJson);
            root = mapper.readTree(requestJson);
            int i = 1;
            for (JsonNode list : root.path("Instances")) {
                System.out.println("Mengambil Gambar BMP " + koneksiDB.URLORTHANC() + ":" + koneksiDB.PORTORTHANC() + "/instances/" + list.asText() + "/preview");
                headers = new HttpHeaders();
                headers.add("Authorization", "Basic " + authEncrypt);
                headers.add("Accept", "image/bmp");
                headers.setAccept(Collections.singletonList(MediaType.APPLICATION_OCTET_STREAM));
                headers.setAccept(Collections.singletonList(MediaType.IMAGE_JPEG));
                HttpEntity<String> entity = new HttpEntity<>(headers);
                ResponseEntity<byte[]> response = getRest().exchange(koneksiDB.URLORTHANC() + ":" + koneksiDB.PORTORTHANC() + "/instances/" + list.asText() + "/preview", HttpMethod.GET, entity, byte[].class);
                Files.write(Paths.get("./gambarradiologi/" + NoRawat + i + ".bmp"), response.getBody());
                i++;
            }
            JOptionPane.showMessageDialog(null, "Pengambilan Gambar BMP dari Orthanc berhasil, silahkan lihat di dalam folder Aplikasi..!!");
        } catch (Exception e) {
            System.out.println("Notifikasi : " + e);
            JOptionPane.showMessageDialog(null, "Gagal mengambil Gambar BMP dari Orthanc, silahkan hubungi administrator ..!!");
        }
        return root;
    }

    public JsonNode AmbilDcm(String NoRawat, String Series) {
        System.out.println("Percobaan Mengambil Gambar DCM : " + NoRawat + ", Series : " + Series);
        try {
            headers = new HttpHeaders();
            System.out.println("Auth : " + authEncrypt);
            headers.add("Authorization", "Basic " + authEncrypt);
            requestEntity = new HttpEntity(headers);
            System.out.println("URL : " + koneksiDB.URLORTHANC() + ":" + koneksiDB.PORTORTHANC() + "/series/" + Series);
            requestJson = getRest().exchange(koneksiDB.URLORTHANC() + ":" + koneksiDB.PORTORTHANC() + "/series/" + Series, HttpMethod.GET, requestEntity, String.class).getBody();
            System.out.println("Result JSON : " + requestJson);
            root = mapper.readTree(requestJson);
            int i = 1;
            for (JsonNode list : root.path("Instances")) {
                System.out.println("Mengambil Gambar DCM " + koneksiDB.URLORTHANC() + ":" + koneksiDB.PORTORTHANC() + "/instances/" + list.asText() + "/file");
                headers = new HttpHeaders();
                headers.add("Authorization", "Basic " + authEncrypt);
                headers.setAccept(Collections.singletonList(MediaType.APPLICATION_OCTET_STREAM));
                headers.setAccept(Collections.singletonList(MediaType.IMAGE_JPEG));
                HttpEntity<String> entity = new HttpEntity<>(headers);
                ResponseEntity<byte[]> response = getRest().exchange(koneksiDB.URLORTHANC() + ":" + koneksiDB.PORTORTHANC() + "/instances/" + list.asText() + "/file", HttpMethod.GET, entity, byte[].class);
                Files.write(Paths.get("./gambarradiologi/" + NoRawat + i + ".dcm"), response.getBody());
                i++;
            }
            JOptionPane.showMessageDialog(null, "Pengambilan Gambar DCM dari Orthanc berhasil, silahkan lihat di dalam folder Aplikasi..!!");
        } catch (Exception e) {
            System.out.println("Notifikasi : " + e);
            JOptionPane.showMessageDialog(null, "Gagal mengambil Gambar DCM dari Orthanc, silahkan hubungi administrator ..!!");
        }
        return root;
    }

    public JsonNode UploadInstance(File file) {
        System.out.println("Percobaan Upload DICOM ke Orthanc : " + file.getName());
        try {
            byte[] data = FileUtils.readFileToByteArray(file);
            headers = new HttpHeaders();
            headers.add("Authorization", "Basic " + authEncrypt);
            headers.setContentType(MediaType.valueOf("application/dicom"));
            HttpEntity<byte[]> entity = new HttpEntity<>(data, headers);
            System.out.println("URL : " + koneksiDB.URLORTHANC() + ":" + koneksiDB.PORTORTHANC() + "/instances");
            requestJson = getRest().exchange(koneksiDB.URLORTHANC() + ":" + koneksiDB.PORTORTHANC() + "/instances", HttpMethod.POST, entity, String.class).getBody();
            System.out.println("Result Upload : " + requestJson);
            root = mapper.readTree(requestJson);
        } catch (Exception e) {
            System.out.println("Notifikasi Upload : " + e);
            root = null;
        }
        return root;
    }

//    private void uploadImage(String fileName, String docPath) {
//    try {
//        File file = new File("./gambarradiologi/" + fileName);
//        byte[] data = FileUtils.readFileToByteArray(file);
//        HttpClient httpClient = new DefaultHttpClient();
//        
//        // Construct the URL for the PHP script
//        String url = "http://" + koneksiDB.HOSTHYBRIDWEB() + ":" + koneksiDB.PORTWEB() + "/" + koneksiDB.HYBRIDWEB() + "/radiologi/upload.php?doc=" + docPath;
//        HttpPost postRequest = new HttpPost(url);
//
//        // Create the multipart entity and add the file part
//        MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
//        ByteArrayBody fileBody = new ByteArrayBody(data, fileName);
//        reqEntity.addPart("file", fileBody);
//
//        // Set the entity to the POST request
//        postRequest.setEntity(reqEntity);
//
//        // Execute the request
//        HttpResponse response = httpClient.execute(postRequest);
//        System.out.println("Response: " + response.getStatusLine().getStatusCode());
//        
//        // Optional: Handle response
//        if (response.getStatusLine().getStatusCode() == 200) {
//            System.out.println("File successfully uploaded: " + fileName);
//        } else {
//            System.out.println("Failed to upload file: " + fileName);
//        }
//        
//    } catch (Exception e) {
//        System.out.println("Upload error: " + e);
//    }
//}


    public RestTemplate getRest() throws NoSuchAlgorithmException, KeyManagementException {
        sslContext = SSLContext.getInstance("SSL");
        TrustManager[] trustManagers = {
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                    }

                    public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                    }
                }
        };
        sslContext.init(null, trustManagers, new SecureRandom());
        sslFactory = new SSLSocketFactory(sslContext, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        scheme = new Scheme("https", 443, sslFactory);
        factory = new HttpComponentsClientHttpRequestFactory();
        factory.getHttpClient().getConnectionManager().getSchemeRegistry().register(scheme);
        return new RestTemplate(factory);
    }
}

