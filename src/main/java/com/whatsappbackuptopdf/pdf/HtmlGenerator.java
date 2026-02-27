package com.whatsappbackuptopdf.pdf;

import com.whatsappbackuptopdf.model.MessageModel;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class HtmlGenerator {

    private final String nomeRemetente;

    public HtmlGenerator(String nomeRemetente) {
        this.nomeRemetente = nomeRemetente;
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

            // Destaca anexos
            if (conteudo.contains("&lt;attached:")) {
                conteudo = conteudo.replaceAll(
                        "&lt;attached:\\s*([^&]+)&gt;",
                        "<span class='attachment'>&#128206; $1</span>"
                );
            }

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

    private String escaparHtml(String texto) {
        if (texto == null) return "";
        return texto
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}