package com.mvv.bots.vk.main;

import com.mvv.bots.vk.Config;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Client {

    public Client(String[] args){

        String address = "127.0.0.1"; // Адрес сервера
        try {
            // Преобразуем адрес из строки во внутреннее представление
            InetAddress addr = InetAddress.getByName(address);

            // Создаём сокет и подключаем его к серверу
            System.out.println("Поключаемся к " + address + ":" + Config.PORT + "...");
            Socket socket = new Socket(addr, Config.PORT);

            // Потоки ввода/вывода
            // BufferedReader позволяет читать вход по строкам
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            // PrintWriter позволяет использовать println
            PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));

            System.out.print("[Запрос]:");
            String line = args[0];

            // Отправляем строку серверу
            out.println(line);
            out.flush(); 	// принудительная отправка

            // Получаем ответ
            line = in.readLine(); // ждем пока сервер отошлет строку текста.
            System.out.println("[Ответ]:" + line);

            // Закрываем соединение
            socket.close();
        } catch (IOException x) {
            x.printStackTrace();
        }
    }
}