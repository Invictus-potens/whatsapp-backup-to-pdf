package com.whatsappbackuptopdf.parser;

import com.whatsappbackuptopdf.model.MessageModel;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatParser {

    // Android: "dd/MM/yy, HH:mm - Author: Message"
    private static final Pattern PATTERN_A = Pattern.compile(
            "^(\\d{1,2}/\\d{1,2}/\\d{2,4},\\s\\d{1,2}:\\d{2}(?::\\d{2})?(?:\\s?(?:AM|PM|am|pm))?)\\s-\\s([^:]+):\\s(.*)",
            Pattern.CASE_INSENSITIVE
    );

    // iPhone: "[dd/MM/yy, HH:mm:ss AM/PM] Author: Menssage"
    private static final Pattern PATTERN_B = Pattern.compile(
            "^\\[(\\d{1,2}/\\d{1,2}/\\d{2,4},\\s\\d{1,2}:\\d{2}:\\d{2}[\\s\\u202F]?(?:AM|PM|am|pm)?)\\]\\s([^:]+):\\s(.*)",
            Pattern.CASE_INSENSITIVE
    );

    private void salvarMensagem(List<MessageModel> mensagens, String ultimaData,
                                String ultimoAutor, StringBuilder mensagemAcumulada) {
        if (!mensagemAcumulada.isEmpty() && !ultimaData.isEmpty()) {
            String[] partes = ultimaData.split(", ");
            String data = partes[0];
            String hora = (partes.length > 1) ? partes[1] : "";

            mensagens.add(new MessageModel(
                    data,
                    hora,
                    ultimoAutor,
                    mensagemAcumulada.toString()
            ));

            mensagemAcumulada.setLength(0);
        }
    }

    private List<String> lerLinhas(String caminho) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(caminho));

        if (bytes.length >= 2 &&
                ((bytes[0] == (byte) 0xFF && bytes[1] == (byte) 0xFE) ||
                        (bytes[0] == (byte) 0xFE && bytes[1] == (byte) 0xFF))) {
            String conteudo = new String(bytes, StandardCharsets.UTF_16);
            return List.of(conteudo.split("\\r?\\n"));
        }

        String conteudo = new String(bytes, StandardCharsets.UTF_8);
        return List.of(conteudo.split("\\r?\\n"));
    }

    private String limparLinha(String linha) {
        return linha
                .replace("\uFEFF", "")   // BOM UTF-8
                .replace("\u200E", "")   // LRM - Left-to-Right Mark
                .replace("\u200F", "")   // RLM - Right-to-Left Mark
                .replace("\u202A", "")   // LRE
                .replace("\u202B", "")   // RLE
                .replace("\u202C", "")   // PDF
                .replace("\u202D", "")   // LRO
                .replace("\u202E", "")   // RLO
                .replace("\u200B", "")   // Zero-Width Space
                .replace("\u2060", "")   // Word Joiner
                .replace("\u202F", "")
                .replaceAll("[\\p{Cf}]", "")
                .trim();
    }

    public List<MessageModel> parse(String caminhoDoArquivo) throws IOException {
        List<MessageModel> mensagens = new ArrayList<>();
        StringBuilder mensagemAcumulada = new StringBuilder();
        String ultimoAutor = "";
        String ultimaData = "";

        List<String> linhas = lerLinhas(caminhoDoArquivo);

        for (String linha : linhas) {
            String linhaLimpa = limparLinha(linha);

            if (linhaLimpa.isEmpty()) continue;

            Matcher matcher = PATTERN_A.matcher(linhaLimpa);
            boolean matched = matcher.find();

            if (!matched) {
                matcher = PATTERN_B.matcher(linhaLimpa);
                matched = matcher.find();
            }

            if (matched) {
                salvarMensagem(mensagens, ultimaData, ultimoAutor, mensagemAcumulada);
                ultimaData = matcher.group(1);
                ultimoAutor = matcher.group(2);
                mensagemAcumulada.append(matcher.group(3));
            } else {
                if (!mensagemAcumulada.isEmpty()) {
                    mensagemAcumulada.append("\n").append(linhaLimpa);
                }
            }
        }

        salvarMensagem(mensagens, ultimaData, ultimoAutor, mensagemAcumulada);
        return mensagens;
    }
}