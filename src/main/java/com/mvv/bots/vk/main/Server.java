package com.mvv.bots.vk.main;

import com.mvv.bots.vk.Config;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    public Server(String[] args)    {

        try {
            // Создаём сокет и связываем с портом
            ServerSocket sock = new ServerSocket(Integer.parseInt(System.getenv("PORT")));

            // Приостанавливаем программу, пока кто-то не подключится
            // Для подключившегося клиента создаётся ещё один сокет.
            // Это удобно, так как тогда можно работать с клиентом в другом потоке,
            // а сервер будет ждать ещё кого-то.
            System.out.println("Ожидание подключений...");
            Socket client = sock.accept();
            System.out.println("Подключился клиент");

            // Входной и выходной потоки для приёма и передачи данных 
            InputStream sin = client.getInputStream();
            OutputStream sout = client.getOutputStream();

            // Для удобства приёма и отправки данных преобразуем в тип Data*Stream
            DataInputStream in = new DataInputStream(sin);
            DataOutputStream out = new DataOutputStream(sout);

            // Получаем строки с числами и отправляем назад их квадраты
            while(true) {
                String line = in.readUTF();
                System.out.println("Получена строка: " + line);
                out.writeUTF("OK");
                out.flush(); // Принудительная отправка данных из буфера отправки (можно и без этой команды)
            }
        } catch(IOException ex) {
            ex.printStackTrace();
        }
    }
}