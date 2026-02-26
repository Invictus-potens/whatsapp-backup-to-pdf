public class ChatParser {

    private static final Pattern PATTERN = Pattern.compile(
            "^\u200E?\\[(\\d{2}/\\d{2}/\\d{2},\\s\\d{1,2}:\\d{2}:\\d{2}\\s?[AP]M)\\]\\s([^:]+):\\s(?:\u200E?<attached:\\s([^>]+)>|(.*))"
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

        List<String> linhas = Files.readAllLines(
                Paths.get(caminhoDoArquivo),
                StandardCharsets.UTF_8
        );

        for (String linha : linhas) {
            String linhaLimpa = linha.replaceAll("\u200E", "");
            Matcher matcher = PATTERN.matcher(linhaLimpa);

            if (matcher.find()) {

                salvarMensagem(mensagens, ultimaData, ultimoAutor, mensagemAcumulada);

                ultimaData = matcher.group(1);
                ultimoAutor = matcher.group(2);

                String conteudo = (matcher.group(3) != null)
                        ? "[ANEXO]: " + matcher.group(3)
                        : matcher.group(4);

                mensagemAcumulada.append(conteudo);
            } else {
                if (mensagemAcumulada.length() > 0) {
                    mensagemAcumulada.append("\n").append(linhaLimpa);
                }
            }
        }
        salvarMensagem(mensagens, ultimaData, ultimoAutor, mensagemAcumulada);
        // 🤔 o que falta aqui antes de retornar?

        return mensagens;
    }
}