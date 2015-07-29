package ir.NeyTech.PasteBin;

import javax.net.ssl.HttpsURLConnection;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

public class Main {
    static int qr = 0;
    String paste_url = null;

    public static void main(String[] args) throws IOException {

        String text = null, name = null, format = "text", private_level = "0", expire = "N", file_name = null;


        //help
        if (args.length <= 0) {
            show_help();
        }

        // arg
        int counter = 0;
        for (String s : args) {
            if (Objects.equals(s, "-c")) {
                text = args[counter + 1];
            }
            if (Objects.equals(s, "-t")) {
                name = args[counter + 1];
            }
            if (Objects.equals(s, "-f")) {
                format = args[counter + 1];
            }
            if (Objects.equals(s, "-p")) {
                private_level = args[counter + 1];
            }
            if (Objects.equals(s, "-e")) {
                expire = args[counter + 1];
            }
            if (Objects.equals(s, "-i")) {
                file_name = args[counter + 1];
            }
            if (Objects.equals(s, "-h")) {
                show_help();
            }

            if (Objects.equals(s, "-qr")) {
                qr = 1;
            }
            counter++;
        }


        Main main = new Main();


        String urlParameters = "api_option=paste&api_dev_key=c0311f46e7447bf10b10463b550d74d7&api_paste_format=" + format + "&api_paste_private=" + private_level + "&api_paste_expire_date=" + expire;


        if (text != null) {
            urlParameters += "&api_paste_code=" + URLEncoder.encode(text);
        } else if (file_name != null) {
            String file_text = readFile(file_name, Charset.forName("UTF-8"));
            urlParameters += "&api_paste_code=" + URLEncoder.encode(file_text);
        }
        if (name != null) {
            urlParameters += "&api_paste_name=" + URLEncoder.encode(name);
        }

        try {
            main.sendPost(urlParameters);
            if (qr == 1) {
                main.qrcode();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    static String readFile(String path, Charset encoding)
            throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }

    public static void show_help() {
        System.out.println("NAME\n" +
                "       pastebin - command-line pastebin client\n" +
                "\n" +
                "SYNOPSIS\n" +
                "       pastebinit [-cfhitpe(qr)]\n" +
                "\n" +
                "DESCRIPTION\n" +
                "       This manual page documents briefly the pastebin commands\n" +
                "\n" +
                "       pastebinit reads text and sends it to a \"pastebin\" on the internet,\n" +
                "       returning the URL to the user.\n" +
                "\n" +
                "       (SOON)It allows the text to be passed through a pipe (|) or from a file\n" +
                "       passed as an argument.\n" +
                "\n" +
                "OPTIONS\n" +
                "   Optional arguments\n" +
                "\n" +
                "       -c [input text] (required)\n" +
                "\n" +
                "       -f [format for syntax-highlighting] (default: text) (check pastebin's\n" +
                "       website for complete list, example: python)\n" +
                "\n" +
                "       -h Help screen\n" +
                "\n" +
                "       -i [filename] Use filename for input\n" +
                "\n" +
                "       -t [title of paste] (default: none)\n" +
                "       \n" +
                "       -p [private level] 0 = public(default) , 1 = Unlisted , 2 = Private)\n" +
                "\n" +
                "       -e [Expire date] N = Never(default) , 10M = 10 Minutes , 1H = 1 Hour\n" +
                "        , 1D = 1 Day , 1W = 1 Week , 2W = 2 Weeks , 1M = 1 Month\n" +
                "       -qr [generate QR-Code] make a link with QR code.\n" +
                "\n" +
                "AUTHORS\n" +
                "       Pastebin is currently written by Hamid Reza Kaveh Pishghadam.\n" +
                "\n" +
                "       E-mail: hamidsoft12@gmail.com\n" +
                "\n" +
                "COPYRIGHT\n" +
                "       Copyright © 2015 NeyTech Corp.");
    }

    public static String getBetween(String str, String open, String close) {
        if (str == null || open == null || close == null) {
            return null;
        }
        int start = str.indexOf(open);
        if (start != -1) {
            int end = str.indexOf(close, start + open.length());
            if (end != -1) {
                return str.substring(start + open.length(), end);
            }
        }
        return null;
    }

    private void sendPost(String urlParameters) throws Exception {

        String url = "https://pastebin.com/api/api_post.php";
        URL obj = new URL(url);
        HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

        //add reuqest header
        con.setRequestMethod("POST");
        String USER_AGENT = "Mozilla/5.0";
        con.setRequestProperty("User-Agent", USER_AGENT);
        con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

        // Send post request
        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes(urlParameters);
        wr.flush();
        wr.close();

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        //print result
        if (response.toString().length() > 0) {
            paste_url = response.toString();
            System.out.println("Paste URL ==> " + response.toString());
            StringSelection stringSelection = new StringSelection(response.toString());
            Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
            clpbrd.setContents(stringSelection, null);
            System.out.println("URL copied to Clipboard!!");

        }
    }

    private void qrcode() throws Exception {

        String url = "http://api.yon.ir/?url=" + URLEncoder.encode("https://chart.googleapis.com/chart?chs=300x300&cht=qr&chl=" + paste_url + "&choe=UTF-8" + "&format=text");

        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        // optional default is GET
        con.setRequestMethod("GET");

        //add request header
        String USER_AGENT = "Mozilla/5.0";
        con.setRequestProperty("User-Agent", USER_AGENT);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        //print result

        String qrcode = getBetween(response.toString(), "\"output\":\"", "\",\"clicks\"");
        System.out.println("QR-Code URL ==> Yon.ir/" + qrcode);

    }
}
