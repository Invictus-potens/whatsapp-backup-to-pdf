package com.whatsappbackuptopdf.parser;
import com.whatsappbackuptopdf.model.MessageModel; // O seu modelo
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ChatParser {


    private static final Pattern PATTERN = Pattern.compile(
            "^(\\d{1,2}/\\d{1,2}/\\d{2,4},\\s\\d{1,2}:\\d{2}\\s?[\\p{Z}\\s]?(?:AM|PM|am|pm))\\s-\\s([^:]+):\\s(.*)",
            Pattern.CASE_INSENSITIVE
    );

    private void salvarMensagem(List<MessageModel> mensagens, String ultimaData,
                                String ultimoAutor, StringBuilder mensagemAcumulada) {
        if (mensagemAcumulada.length() > 0) {
            String[] partes = ultimaData.split(", ");
            mensagens.add(new MessageModel(
                    partes[0],
                    partes[1],
                    ultimoAutor,
                    mensagemAcumulada.toString()
            ));
            mensagemAcumulada.setLength(0);
        }
    }

    public List<MessageModel> parse(String caminhoDoArquivo) throws IOException {
        List<MessageModel> mensagens = new ArrayList<>();
        StringBuilder mensagemAcumulada = new StringBuilder();
        String ultimoAutor = "";
        String ultimaData = "";

        List<String> linhas = Files.readAllLines(Paths.get(caminhoDoArquivo), StandardCharsets.UTF_8);

        for (String linha : linhas) {

            String linhaLimpa = linha.replaceAll("\u200E", "").trim();
            if (linhaLimpa.isEmpty()) continue;

            Matcher matcher = PATTERN.matcher(linhaLimpa);

            if (matcher.find()) {

                salvarMensagem(mensagens, ultimaData, ultimoAutor, mensagemAcumulada);

                ultimaData = matcher.group(1);
                ultimoAutor = matcher.group(2);


                mensagemAcumulada.append(matcher.group(3));
            } else {

                if (mensagemAcumulada.length() > 0) {
                    mensagemAcumulada.append("\n").append(linhaLimpa);
                }
            }
        }

        salvarMensagem(mensagens, ultimaData, ultimoAutor, mensagemAcumulada);

        return mensagens;
    }
}
