/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package bootstrap;

import common.BootstrapRemote;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class BootstrapNode {

    public static void main(String[] args) {

        try {

            System.setProperty(
                    "java.rmi.server.hostname",
                    "127.0.0.1"
            );

            Registry registry;

            try {

                registry = LocateRegistry.createRegistry(1099);

                System.out.println(
                        "RMI Registry created on port 1099."
                );

            } catch (Exception e) {

                registry = LocateRegistry.getRegistry(
                        "127.0.0.1",
                        1099
                );

                System.out.println(
                        "Using existing RMI Registry on port 1099."
                );
            }

            BootstrapRemote service =
                    new BootstrapService();

            registry.rebind(
                    "BootstrapService",
                    service
            );

            System.out.println(
                    "================================="
            );
            System.out.println(
                    "Bootstrap Node is running."
            );
            System.out.println(
                    "Registry Port: 1099"
            );
            System.out.println(
                    "================================="
            );

            while (true) {
                Thread.sleep(10000);
            }

        } catch (Exception e) {

            System.out.println(
                    "Bootstrap error:"
            );

            e.printStackTrace();
        }
    }
}