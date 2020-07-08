import java.net.*;
import java.io.*;
import java.util.Scanner;

public class Client{
    private DatagramSocket clientSocket = null;
    private Scanner sc = null;
    private byte[] buffer;
    private String strComunicacao;
    private int portaComunicacao;

    public Client(int porta){
        try{
            portaComunicacao = 5000;
            clientSocket = new DatagramSocket(porta);
            buffer = new byte[65507];
            clientSocket.setSoTimeout(9000);
            strComunicacao = "";
            sc = new Scanner(System.in);
            start();
        }
        catch(IOException ioe){
            System.out.println(ioe);
        }
    }

    /* Método utilizado para que o cliente receba a mesagem: Fim do programa, enviada pelo servidor
       quando o cliente desejar desconectar.
    */
    public void fim(){
        strComunicacao = "";
        try{
            DatagramPacket datagramPacket = new DatagramPacket(buffer, 0,buffer.length);
            clientSocket.receive(datagramPacket);
            strComunicacao = new String(datagramPacket.getData());
            System.out.println(strComunicacao);
            buffer = new byte[65507];
        }
        catch(IOException ioe){
            System.out.println(ioe);
        }
    }

    /* Método utilzado para receber o resultado das informações pesquisadas pelo servidor
       do menor preço de algum tipo de combustível, solicitado pelo cliente
    */
    public void recebePesquisa(){
        strComunicacao = "";
        try{
            DatagramPacket datagramPacket1 = new DatagramPacket(buffer, 0,buffer.length);
            clientSocket.receive(datagramPacket1);
            strComunicacao = new String(datagramPacket1.getData());
            System.out.print(strComunicacao);
            buffer = new byte[65507];
        }
        catch(IOException ioe){
            System.out.println(ioe);
        }
    }

    public void start(){
        String line = "";
        /* Quando o cliente digita Fim, as atividades do cliente são encerradas.
           Quando as atividades do cliente são encerradas, outro cliente pode se conectar com
           o servidor, e realizar suas solicitações de pesquisa ou entradas de dados
        */
        while(!line.equals("Fim")){
            try{
                System.out.println("Dgite D para informar um dado ou P para pesquisar\nDigite um identificador\nDigite um tipo decombustivel:\t0 - diesel, 1 - alcool, 2- gasolina\nDigite um preco e as coordendas do posto (latitude e longitude) ou o raio de busca e as coordenadas do centro de busca (latitude e longitude):");
                line = sc.nextLine();
                DatagramPacket datagramPacket = new DatagramPacket(line.getBytes(),line.length(),InetAddress.getLocalHost(),portaComunicacao);
                clientSocket.send(datagramPacket);
                if(line.charAt(0)=='P'){
                    recebePesquisa();
                }
            }
            catch(IOException ioe){
                System.out.println(ioe);
            }
        }
        if(line.equals("Fim")){
            fim();
        }
        close();
    }

    /* Método utilizado para encerramento das atividades do cliente
    */
    public void close(){
        sc.close();
        clientSocket.close();
    }

    public static void main(String args[]){
        new Client(5001);
    }
}