import java.net.*;
import java.io.*;

public class Server{
    private DatagramSocket serverSocket = null;
    private String strComunicacao;
    private String line;
    private String strResposta;
    private int clientPort;
    private byte[] buffer;

    public Server(int porta, int client){
        try{
            clientPort = client;
            serverSocket = new DatagramSocket(porta);
            buffer = new byte[65507];
            strComunicacao = "";
            line = "";
            strResposta = " ";
            start();
        }
        catch(IOException ioe){
            System.out.println(ioe);
        }
    }

    /* Método que envia o resultado da busca do menor preço de um determinado tipo de combustível
       dentro de um raio determinado
    */
    public void enviaInformacoes(int tipo, int raio, int latitude, int longitude){
        int tamanhoDaMensagem = 0;
        strComunicacao = "";
        strResposta = "";
        double distancia;
        /* As variáveis lat e longit correspondem à latitude e à longitude de um ponto, inicialmente
           com os valores do ponto central menos o tamnho do raio. Com o intuito de pegar o limite
           mais inferior possível, se visualizássemos um retangulo ao invés de uma circunferência
           A diferença é que o retangulo englabaria mais pontos do que a circunferência devido às pontas
           do retângulo que abrangem uma área maior.
           Porém, este ponto recebe este valor inicial apenas para termos uma referência de onde começar os
           cálculos. Mas apenas os pontos dentro da circunferência serão considerados.
        */
        int longit=longitude-raio,lat;
        String nomeArquivo,localPosto = "", diesel = "",alcool = "", gasolina = "";
        try{
            while(longit <= (raio+longitude)){
                lat = latitude - raio;
                /* É efetuado um cálculo de distância do ponto atual até o ponto do centro da circunferência.
                   O intuito é verificar se o ponto atual, de coordenadas (latitude,longitude), se encontra
                   dentro da área da circunferência.
                */
                distancia = Math.sqrt(Math.pow(lat-latitude,2)+(Math.pow(longit-longitude,2)));
                /* Caso a distância entre o ponto atual e o centro da circunferência for maior que o raio,
                   neste caso, o ponto se encontra fora da circunferência. Devido a isso, incrementamos o valor
                   até encontrarmos um ponto dentro da circunferência
                */
                while(distancia > raio){
                    lat++;
                    distancia = Math.sqrt(Math.pow(lat-latitude,2)+(Math.pow(longit-longitude,2)));
                }
                /* Quando a distância entre o ponto atual e o centro da circunferência for igual ao raio,
                   temos que o ponto atual encontra-se na borda da circunferência. E este também será considerado.
                   Caso o ponto atual em relação ao centro da circunferência, esteja a uma distância menor que
                   o raio, este ponto se encontra dentro da circunferência.
                */
                while(distancia <= raio){
                    nomeArquivo = "Posto: ";
                    nomeArquivo = nomeArquivo + Integer.toString(lat);
                    nomeArquivo = nomeArquivo + "," + Integer.toString(longit) + ".txt";
                    /* Pesquisamos se existe algum posto de combustível nessas coordenadas de latitude e longitude
                    cadastrado no banco de dados do servidor.
                    */
                    File file = new File(nomeArquivo);
                    if(file.exists()){
                        /* Caso haja algum registro deste posto de combustíveis, seus dados são recuperados
                        */
                        FileReader fr = new FileReader(file);
                        BufferedReader buffered=new BufferedReader(fr);
                        localPosto = localPosto + buffered.readLine() + "\t";
                        diesel = diesel + buffered.readLine() + "\t";
                        alcool = alcool + buffered.readLine() + "\t";
                        gasolina = gasolina + buffered.readLine() + "\t";
                        buffered.close();
                    }
                    lat++;
                    distancia = Math.sqrt(Math.pow(lat-latitude,2)+(Math.pow(longit-longitude,2)));
                }
                longit++;
            }
            double menorPreco, precoAux;
            String locaisPostos[] = localPosto.split("\t");
            String dieselEncontrados[] = diesel.split("\t");
            String alcoolEncontrados[] = alcool.split("\t");
            String gasolinaEncontradas[] = gasolina.split("\t");
            /* Caso algum posto de combustível cadastrado esteja dentro daquele raio de busca a partir do ponto central
               informado pelo cliente.
            */
            if(localPosto.length() >= 1){
                /* Caso o tipo de combustível solicitado pelo cliente para que seja feita a pesquisa
                   de menor preço seja um combustível do tipo Diesel
                */
                if(tipo == 0){
                    int j=0;
                    String precoDiesel1[] = dieselEncontrados[j].split(" ");
                    /* Dentre os postos de combustíveis encontrados, alguns podem conter informações
                       que ainda não foram informadas, devido a isso, descartamos os postos de combustíveis
                       que ainda possuem informações de preço desconhecido sobre o tipo de combustível
                       solicitado pelo cliente para a pesquisa de menor preço.
                    */
                    while(j < dieselEncontrados.length && precoDiesel1[2].contains("desconhecido")){
                        precoDiesel1 = dieselEncontrados[j].split(" ");
                        j++;
                    }
                    /* Caso o primeiro posto de combustível já possua uma informação conhecida,
                       não devemos decrementar o valor do índice, pois ele indica o índice do primiero
                       posto de combustível que possui um preço conhecido do tipo de combustível desejado
                       pelo cliente para que seja feita a pesquisa de menor preço.
                       O valor do índice só é decrementado, pois ao sair do comando while acima, o índice
                       estará com o valor correspondente ao próximo posto de combustível. Não sendo o primeiro
                       posto que contém informações conhecidas.
                    */
                    if(j != 0){
                        j--;
                    }
                    /* Ao encontrarmos o primeiro posto de combustíveis que possua um informação conhecida
                       sobre o determinado tipo de combustível solicitado para busca do menor valor,
                       atribuímos este preço inicial para a variável menorPreco, apenas para termos uma referência
                       de comparação com os demais valores.
                    */
                    if(!precoDiesel1[2].contains("desconhecido")){
                        strResposta = locaisPostos[j] + "\n";
                        strResposta = strResposta + dieselEncontrados[j] + "\n\n";
                        menorPreco = Double.parseDouble(precoDiesel1[2]);
                        for(int i = j + 1 ; i < locaisPostos.length; i++){
                            String precoDiesel2[] = dieselEncontrados[i].split(" ");
                            /* Caso encontremos outro valor conhecido, verificamos se este novo valor
                               é menor do que o valor contido na variável menorPreco.
                            */
                            if(!precoDiesel2[2].contains("desconhecido")){
                                precoAux = Double.parseDouble(precoDiesel2[2]);
                                /* Se os valores forem iguais, o servidor informará ao cliente todos os
                                   postos de combustíveis que possuem o menor preço daquele tipo de combustível.
                                   E informará também o valor encontrado.
                                */
                                if(menorPreco == precoAux){
                                    strResposta = strResposta + locaisPostos[i] + "\n";
                                    strResposta = strResposta + dieselEncontrados[i] + "\n\n";
                                }
                                /* Caso contrário, o servidor informará ao cliente apenas o posto de combustível
                                   que possui o menor preço daquele tipo de combustível. E informará também o valor
                                   encontrado.
                                */
                                else if(menorPreco > precoAux){
                                    menorPreco = precoAux;
                                    strResposta = locaisPostos[i] + "\n";
                                    strResposta = strResposta + dieselEncontrados[i] + "\n\n";
                                }
                            }
                        }
                    }
                }
                /* Caso o tipo de combustível solicitado pelo cliente para que seja feita a pesquisa
                   de menor preço seja um combustível do tipo Álcool
                */
                else if(tipo == 1){
                    int j=0;
                    String precoAlcool1[] = alcoolEncontrados[j].split(" ");
                    /* Dentre os postos de combustíveis encontrados, alguns podem conter informações
                       que ainda não foram informadas, devido a isso, descartamos os postos de combustíveis
                       que ainda possuem informações de preço desconhecido sobre o tipo de combustível
                       solicitado pelo cliente para a pesquisa de menor preço.
                    */
                    while(j < alcoolEncontrados.length && precoAlcool1[2].contains("desconhecido")){
                        precoAlcool1 = alcoolEncontrados[j].split(" ");
                        j++;
                    }
                    /* Caso o primeiro posto de combustível já possua uma informação conhecida,
                       não devemos decrementar o valor do índice, pois ele indica o índice do primiero
                       posto de combustível que possui um preço conhecido do tipo de combustível desejado
                       pelo cliente para que seja feita a pesquisa de menor preço.
                       O valor do índice só é decrementado, pois ao sair do comando while acima, o índice
                       estará com o valor correspondente ao próximo posto de combustível. Não sendo o primeiro
                       posto que contém informações conhecidas.
                    */
                    if(j != 0){
                        j--;
                    }
                    /* Ao encontrarmos o primeiro posto de combustíveis que possua um informação conhecida
                       sobre o determinado tipo de combustível solicitado para busca do menor valor,
                       atribuímos este preço inicial para a variável menorPreco, apenas para termos uma referência
                       de comparação com os demais valores.
                    */
                    if(!precoAlcool1[2].contains("desconhecido")){
                        strResposta = locaisPostos[j] + "\n";
                        strResposta = strResposta + alcoolEncontrados[j] + "\n\n";
                        menorPreco = Double.parseDouble(precoAlcool1[2]);
                        for(int i = j + 1 ; i < locaisPostos.length; i++){
                            String precoAlcool2[] = alcoolEncontrados[i].split(" ");
                            /* Caso encontremos outro valor conhecido, verificamos se este novo valor
                               é menor do que o valor contido na variável menorPreco.
                            */
                            if(!precoAlcool2[2].contains("desconhecido")){
                                precoAux = Double.parseDouble(precoAlcool2[2]);
                                /* Se os valores forem iguais, o servidor informará ao cliente todos os
                                   postos de combustíveis que possuem o menor preço daquele tipo de combustível.
                                   E informará também o valor encontrado.
                                */
                                if(menorPreco == precoAux){
                                    strResposta = strResposta + locaisPostos[i] + "\n";
                                    strResposta = strResposta + alcoolEncontrados[i] + "\n\n";
                                }
                                /* Caso contrário, o servidor informará ao cliente apenas o posto de combustível
                                   que possui o menor preço daquele tipo de combustível. E informará também o valor
                                   encontrado.
                                */
                                else if(menorPreco > precoAux){
                                    menorPreco = precoAux;
                                    strResposta = locaisPostos[i] + "\n";
                                    strResposta = strResposta + alcoolEncontrados[i] + "\n\n";
                                }
                            }
                        }
                    }
                }
                /* Caso o tipo de combustível solicitado pelo cliente para que seja feita a pesquisa
                   de menor preço seja um combustível do tipo Gasolina
                */
                else if(tipo == 2){
                    int j=0;
                    String precoGasolina1[] = gasolinaEncontradas[j].split(" ");
                    /* Dentre os postos de combustíveis encontrados, alguns podem conter informações
                       que ainda não foram informadas, devido a isso, descartamos os postos de combustíveis
                       que ainda possuem informações de preço desconhecido sobre o tipo de combustível
                       solicitado pelo cliente para a pesquisa de menor preço.
                    */
                    while(j < gasolinaEncontradas.length && precoGasolina1[2].contains("desconhecido")){
                        precoGasolina1 = gasolinaEncontradas[j].split(" ");
                        j++;
                    }
                    /* Caso o primeiro posto de combustível já possua uma informação conhecida,
                       não devemos decrementar o valor do índice, pois ele indica o índice do primiero
                       posto de combustível que possui um preço conhecido do tipo de combustível desejado
                       pelo cliente para que seja feita a pesquisa de menor preço.
                       O valor do índice só é decrementado, pois ao sair do comando while acima, o índice
                       estará com o valor correspondente ao próximo posto de combustível. Não sendo o primeiro
                       posto que contém informações conhecidas.
                    */
                    if(j != 0){
                        j--;
                    }
                    /* Ao encontrarmos o primeiro posto de combustíveis que possua um informação conhecida
                       sobre o determinado tipo de combustível solicitado para busca do menor valor,
                       atribuímos este preço inicial para a variável menorPreco, apenas para termos uma referência
                       de comparação com os demais valores.
                    */
                    if(!precoGasolina1[2].contains("desconhecido")){
                        strResposta = locaisPostos[j] + "\n";
                        strResposta = strResposta + gasolinaEncontradas[j] + "\n\n";
                        menorPreco = Double.parseDouble(precoGasolina1[2]);
                        for(int i = j + 1 ; i < locaisPostos.length; i++){
                            String precoGasolina2[] = gasolinaEncontradas[i].split(" ");
                            /* Caso encontremos outro valor conhecido, verificamos se este novo valor
                               é menor do que o valor contido na variável menorPreco.
                            */
                            if(!precoGasolina2[2].contains("desconhecido")){
                                precoAux = Double.parseDouble(precoGasolina2[2]);
                                /* Se os valores forem iguais, o servidor informará ao cliente todos os
                                   postos de combustíveis que possuem o menor preço daquele tipo de combustível.
                                   E informará também o valor encontrado.
                                */
                                if(menorPreco == precoAux){
                                    strResposta = strResposta + locaisPostos[i] + "\n";
                                    strResposta = strResposta + gasolinaEncontradas[i] + "\n\n";
                                }
                                /* Caso contrário, o servidor informará ao cliente apenas o posto de combustível
                                   que possui o menor preço daquele tipo de combustível. E informará também o valor
                                   encontrado.
                                */
                                else if(menorPreco > precoAux){
                                    menorPreco = precoAux;
                                    strResposta = locaisPostos[i] + "\n";
                                    strResposta = strResposta + gasolinaEncontradas[i] + "\n\n";
                                }
                            }
                        }
                    }
                }
                tamanhoDaMensagem = strResposta.length();
            }
            /* Caso nenhum posto de combustível cadastrado esteja dentro daquele raio de busca
               a partir do ponto central informado pelo cliente.
            */
            if(localPosto.length() < 1){
                strResposta = "Não foram encontrados postos de combustível dentro de um raio de "+ Integer.toString(raio)+"\n\n";
                tamanhoDaMensagem = strResposta.length()+2;
            }
            /* Caso todos os posto de combustíveis cadastrados possuam informações desconhecidas
               na qual o cliente solicitou pesquisar o menor preço
            */
            else if(tamanhoDaMensagem == 0){
                strResposta = "Não foram encontrados postos de combustível com preços conhecidos sobre este tipo de combustível\n\n";
                tamanhoDaMensagem = strResposta.length()+4;
            }
            DatagramPacket datagramPacket4 = new DatagramPacket(strResposta.getBytes(),tamanhoDaMensagem, InetAddress.getLocalHost(),clientPort);
            serverSocket.send(datagramPacket4);
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    /* Método que recebe o nome do arquivo que deseja ser aberto, para leitura dos dados.
    */
    public String leArquivo(String nomeArquivo){
        String diesel = "", alcool = "", gasolina = "",nomePosto = "", retorno="";
        try{
            File file = new File(nomeArquivo);
            /* Caso o arquivo não exista, ele cria um arquivo contendo as coordenadas do posto e seus
               valores nos combustíveis, inicialmente desconhecidos.
            */
            if(!file.exists()){
                file.createNewFile();
                return "vazio";
            }
            /* Caso o arquivo já exista, ele recupera os dados que estão contidos no arquivo e deleta o mesmo
               pois este arquivo estava desatualizado, e as informações serão atualizadas num novo aquivo com o mesmo nome
            */
            else{
                FileReader fr = new FileReader(file);
                BufferedReader buffered = new BufferedReader(fr);
                nomePosto = buffered.readLine();
                diesel = buffered.readLine();
                alcool = buffered.readLine();
                gasolina = buffered.readLine();
                retorno = nomePosto + "\t" + diesel + "\t" + alcool + "\t" + gasolina;
                buffered.close();
                file.delete();
            }
        }
        catch(IOException e){
            e.printStackTrace();
        }
        return retorno;
    }

    /* Método que irá ler um dado de preço de um determinado tipo de combustível num determinado
       posto de combustível e armazenar esta informação num arquivo referente ao posto em questão
    */
    public void leDados(int tipo, int preco, int latitude, int longitude){
        strComunicacao = "";
        double precoReal = (preco*1.000)/1000;
        String nomeArquivo,nomePosto = "";
        nomeArquivo = "Posto: ";
        nomeArquivo = nomeArquivo + Integer.toString(latitude);
        nomeArquivo = nomeArquivo + "," + Integer.toString(longitude);
        nomePosto = nomeArquivo;
        nomeArquivo = nomeArquivo + ".txt";
        /* O nome do arquivo referente a um determinado posto, consiste em:
           Posto: x,y
           onde x e y correspondem respectivamente às coordenadas de latitude e
           longitude daquele posto em questão.
           Executamos o método leArquivo, para identificarmos se o arquivo já existe ou não
        */
        String resposta = leArquivo(nomeArquivo);
        try{
            File file = new File(nomeArquivo);
            FileWriter fw = new FileWriter(file);
            BufferedWriter bw=new BufferedWriter(fw);
            /* Caso o método leArquivo retorne o valor "vazio" que está sendo atribuído na String
               resposta, isto indica que arquivo nã existia, ou seja, não havia registro algum sobre
               o posto de combustível em questão. Desta forma, suas informações de preço dos tipos de combustíveis
               são inicialemente desconhecidas. Porém, considerando o fato do cliente ter realizado a entrada
               de dados de um dos tipos de combustível daquele posto, o servidor insere esse dado informado
               na linha correspondente a ele. Porém as outras linhas, continuam possuindo informações ainda desconhecidas.
            */
            if(resposta.equals("vazio")){
                /* Caso o cliente tenha informado um dado sobre o preço do diesel do posto em questão
                */
                if(tipo == 0){
                    bw.write(nomePosto + "\n");
                    bw.write("Diesel: R$ " + precoReal + "\n");
                    bw.write("Alcool: preço desconhecido\n");
                    bw.write("Gasolina: preço desconhecido\n");
                }
                /* Caso o cliente tenha informado um dado sobre o preço do álcool do posto em questão
                */
                else if(tipo == 1){
                    bw.write(nomePosto + "\n");
                    bw.write("Diesel: preço desconhecido\n");
                    bw.write("Alcool: R$ " + precoReal + "\n");
                    bw.write("Gasolina: preço desconhecido\n");
                }
                /* Caso o cliente tenha informado um dado sobre o preço da gasolina do posto em questão
                */
                else if(tipo == 2){
                    bw.write(nomePosto + "\n");
                    bw.write("Diesel: preço desconhecido\n");
                    bw.write("Alcool: preço desconhecido\n");
                    bw.write("Gasolina: R$ " + precoReal);
                }
            }
            /* Caso o método leArquivo não retorne o valor "vazio" que está sendo atribuído na String
               resposta, isto indica que arquivo já existia, ou seja, já havia algum registro sobre o
               posto de combustível em questão. Desta forma, suas informações de preço dos tipos de
               combustível são recuperadas. Considerando o fato do cliente ter realizado a entrada
               de dados de um dos tipos de combustível daquele posto, o servidor insere esse dado
               informado na linha correspondente a ele. Ou seja, o servidor irá atualizar as informações
               que estavam armazenadas a respeito do posto de combustível em questão.
            */
            else{
                String informacoes[] = resposta.split("\t");
                /* Caso o cliente tenha informado um dado sobre o preço do diesel do posto em questão
                */
                if(tipo == 0){
                    bw.write(informacoes[0] + "\n");
                    bw.write("Diesel: R$ " + precoReal + "\n");
                    bw.write(informacoes[2] + "\n");
                    bw.write(informacoes[3] + "\n");
                }
                /* Caso o cliente tenha informado um dado sobre o preço do álcool do posto em questão
                */
                else if(tipo == 1){
                    bw.write(informacoes[0] + "\n");
                    bw.write(informacoes[1] + "\n");
                    bw.write("Alcool: R$ " + precoReal + "\n");
                    bw.write(informacoes[3] + "\n");
                }
                /* Caso o cliente tenha informado um dado sobre o preço da gasolina do posto em questão
                */
                else if(tipo == 2){
                    bw.write(informacoes[0] + "\n");
                    bw.write(informacoes[1] + "\n");
                    bw.write(informacoes[2] + "\n");
                    bw.write("Gasolina: R$ " + precoReal);
                }
            }
            bw.close();
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    public void start(){
        try{
            while(true){
                DatagramPacket datagramPacket1 = new DatagramPacket(buffer, buffer.length);
                serverSocket.receive(datagramPacket1);
                line = new String(datagramPacket1.getData());
                buffer = new byte[65507];
                strComunicacao="";
                System.out.println("Mensagem recebida pelo clinte:\n"+line);
                /* Caso o cliente solicite uma pesquisa de menor preço de um determinado tipo de combustível
                */
                if(line.charAt(0) == 'P'){
                    String comando[] = line.split(" ");
                    enviaInformacoes(Integer.parseInt(comando[2]),Integer.parseInt(comando[3]),Integer.parseInt(comando[4]),((int)Float.parseFloat(comando[5])));
                }
                /* Caso o cliente deseje informar dados ao servidor sobre preço de um determinado tipo de combustível
                   num determinado posto de combustível
                */
                else if(line.charAt(0) == 'D'){
                    String comando[] = line.split(" ");
                    leDados(Integer.parseInt(comando[2]),Integer.parseInt(comando[3]),Integer.parseInt(comando[4]),((int)Float.parseFloat(comando[5])));
                }
                else if(line.contains("Fim")){
                    strComunicacao = "Fim do programa";
                    DatagramPacket datagramPacket2 = new DatagramPacket(strComunicacao.getBytes(),strComunicacao.length(),InetAddress.getLocalHost(),clientPort);
                    serverSocket.send(datagramPacket2);
                    strComunicacao="";
                }
            }
        }
        catch(IOException ioe){
            System.out.println(ioe);
        }
    }

    public static void main(String args[]){
        new Server(5000,5001);
    }
}