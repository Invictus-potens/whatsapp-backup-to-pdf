package com.whatsappbackuptopdf.pdf;

import com.whatsappbackuptopdf.model.MessageModel;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;

public class HtmlGenerator {

    // "arquivo.jpg (file attached)" — formato Android
    private static final Pattern PATTERN_FILE_ATTACHED = Pattern.compile("(\\S+\\.\\w+) \\(file attached\\)");
    // "<attached: arquivo.jpg>" — formato iPhone (após escape HTML)
    private static final Pattern PATTERN_ATTACHED_TAG = Pattern.compile("&lt;attached:\\s*([^&]+)&gt;");
    // "<Media omitted>" — mídia não exportada
    private static final Pattern PATTERN_MEDIA_OMITTED = Pattern.compile("&lt;Media omitted&gt;");

    private final String nomeRemetente;
    private final String outputFolderPath;
    private final java.util.Set<String> webpComFalha = new java.util.HashSet<>();

    public HtmlGenerator(String nomeRemetente, String outputFolderPath) {
        this.nomeRemetente = nomeRemetente;
        this.outputFolderPath = outputFolderPath;
    }

    public String gerarHtml(List<MessageModel> mensagens) {
        StringBuilder sb = new StringBuilder();

        sb.append("""
                <!DOCTYPE html>
                <html lang="pt-BR">
                <head>
                    <meta charset="UTF-8"/>
                    <style>
                        * { margin: 0; padding: 0; }

                        body {
                            font-family: Arial, sans-serif;
                            background-color: #e5ddd5;
                            padding: 20px;
                        }

                        .chat-container {
                            max-width: 800px;
                            margin: 0 auto;
                        }

                        /* Separador de data */
                        .date-divider {
                            text-align: center;
                            margin: 12px 0;
                        }

                        .date-divider span {
                            background-color: #e1f2fb;
                            color: #54656f;
                            font-size: 11px;
                            padding: 4px 10px;
                        }

                        /* Row de mensagem usando tabela */
                        .message-row {
                            width: 100%;
                            margin-bottom: 4px;
                            display: block;
                        }

                        /* Lado esquerdo (recebida) */
                        .message-row.received .bubble-wrap {
                            float: left;
                            max-width: 65%;
                        }

                        /* Lado direito (enviada) */
                        .message-row.sent .bubble-wrap {
                            float: right;
                            max-width: 65%;
                        }

                        .clearfix {
                            display: block;
                            clear: both;
                        }

                        .author-name {
                            font-size: 11px;
                            font-weight: bold;
                            color: #075e54;
                            margin-bottom: 2px;
                        }

                        .bubble {
                            padding: 6px 10px;
                            font-size: 12px;
                            line-height: 1.5;
                            word-wrap: break-word;
                        }

                        .bubble.sent {
                            background-color: #dcf8c6;
                        }

                        .bubble.received {
                            background-color: #ffffff;
                        }

                        .time {
                            font-size: 10px;
                            color: #8696a0;
                            text-align: right;
                            display: block;
                            margin-top: 2px;
                        }

                        .system-message {
                            text-align: center;
                            font-size: 11px;
                            color: #54656f;
                            background-color: #fef9c3;
                            padding: 4px 12px;
                            margin: 6px auto;
                        }

                        .attachment {
                            font-style: italic;
                            color: #8696a0;
                        }

                        .media-image {
                            max-width: 100%;
                            max-height: 300px;
                            display: block;
                            margin: 4px 0;
                        }

                        .media-placeholder {
                            background-color: #f0f0f0;
                            border-radius: 8px;
                            padding: 10px 14px;
                            color: #54656f;
                            font-size: 12px;
                            font-style: italic;
                            display: block;
                            margin: 4px 0;
                        }
                    </style>
                </head>
                <body>
                <div class="chat-container">
                """);

        String ultimaData = "";

        for (MessageModel msg : mensagens) {

            // Separador de data
            if (!msg.getDate().equals(ultimaData)) {
                ultimaData = msg.getDate();
                sb.append(String.format("""
                        <div class="date-divider"><span>%s</span></div>
                        """, escaparHtml(ultimaData)));
            }

            // Mensagem de sistema
            if (msg.getSender() == null || msg.getSender().isBlank()) {
                sb.append(String.format("""
                        <div class="system-message">%s</div>
                        """, escaparHtml(msg.getContent())));
                continue;
            }

            boolean enviada = msg.getSender().trim().equalsIgnoreCase(nomeRemetente.trim());
            String lado = enviada ? "sent" : "received";
            String conteudo = escaparHtml(msg.getContent());

            // Renderiza anexos — formato Android: "arquivo.jpg (file attached)"
            conteudo = aplicarPadrao(conteudo, PATTERN_FILE_ATTACHED);
            // Renderiza anexos — formato iPhone: "<attached: arquivo.jpg>"
            conteudo = aplicarPadrao(conteudo, PATTERN_ATTACHED_TAG);
            // Mídia omitida no export
            conteudo = PATTERN_MEDIA_OMITTED.matcher(conteudo)
                    .replaceAll("<div class='media-placeholder'>&#128247; mídia não incluída no export</div>");

            String autorHtml = (!enviada)
                    ? String.format("<div class='author-name'>%s</div>", escaparHtml(msg.getSender()))
                    : "";

            sb.append(String.format("""
                    <div class="message-row %s">
                        <div class="bubble-wrap">
                            %s
                            <div class="bubble %s">
                                %s
                                <span class="time">%s</span>
                            </div>
                        </div>
                    </div>
                    <div class="clearfix"></div>
                    """,
                    lado,
                    autorHtml,
                    lado,
                    conteudo,
                    escaparHtml(msg.getTime())
            ));
        }

        sb.append("""
                </div>
                </body>
                </html>
                """);

        return sb.toString();
    }

    public void salvarHtml(List<MessageModel> mensagens, String caminhoSaida) throws IOException {
        String html = gerarHtml(mensagens);
        Files.writeString(Paths.get(caminhoSaida), html, StandardCharsets.UTF_8);
        System.out.println("HTML gerado: " + caminhoSaida);
    }

    private String aplicarPadrao(String conteudo, Pattern padrao) {
        Matcher matcher = padrao.matcher(conteudo);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String nomeArquivo = matcher.group(1).trim();
            matcher.appendReplacement(sb, Matcher.quoteReplacement(renderizarAnexo(nomeArquivo)));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private String detectarExtensao(String nomeArquivo) {
        if (nomeArquivo == null || !nomeArquivo.contains(".")) return "";
        return nomeArquivo.substring(nomeArquivo.lastIndexOf('.') + 1).toLowerCase();
    }

    private String renderizarAnexo(String nomeArquivo) {
        String ext = detectarExtensao(nomeArquivo);
        return switch (ext) {
            case "jpg", "jpeg", "png", "gif" ->
                    String.format("<img class='media-image' src='%s' alt='%s'/>", toUriPath(nomeArquivo), nomeArquivo);
            case "webp" -> {
                String pngNome = converterWebpParaPng(nomeArquivo);
                if (pngNome != null) {
                    yield String.format("<img class='media-image' src='%s' alt='%s'/>", toUriPath(pngNome), nomeArquivo);
                }
                yield String.format("<div class='media-placeholder'>&#128247; %s</div>", nomeArquivo);
            }
            case "mp3", "ogg", "aac", "m4a", "opus", "wav" ->
                    String.format("<div class='media-placeholder'>&#127925; %s</div>", nomeArquivo);
            case "mp4", "avi", "mov", "mkv", "3gp" ->
                    String.format("<div class='media-placeholder'>&#127916; %s</div>", nomeArquivo);
            default ->
                    String.format("<span class='attachment'>&#128206; %s</span>", nomeArquivo);
        };
    }

    private String converterWebpParaPng(String nomeArquivo) {
        if (webpComFalha.contains(nomeArquivo)) return null;

        Path webpPath = Paths.get(outputFolderPath, nomeArquivo);
        if (!Files.exists(webpPath)) return null;

        String pngNome = nomeArquivo.substring(0, nomeArquivo.lastIndexOf('.')) + ".png";
        Path pngPath = Paths.get(outputFolderPath, pngNome);

        if (Files.exists(pngPath)) return pngNome;

        try {
            BufferedImage img = ImageIO.read(webpPath.toFile());
            if (img == null) {
                webpComFalha.add(nomeArquivo);
                return null;
            }
            ImageIO.write(img, "PNG", pngPath.toFile());
            return pngNome;
        } catch (IOException e) {
            webpComFalha.add(nomeArquivo);
            System.err.println("Falha ao converter WebP: " + nomeArquivo + " - " + e.getMessage());
            return null;
        }
    }

    private String toUriPath(String filename) {
        try {
            return new java.net.URI(null, null, filename, null).toASCIIString();
        } catch (java.net.URISyntaxException e) {
            return filename;
        }
    }

    private String escaparHtml(String texto) {
        if (texto == null) return "";
        return texto
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
