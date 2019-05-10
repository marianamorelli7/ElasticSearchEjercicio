package app;


import domain.Item;
import service.*;
import java.io.IOException;
import java.util.Scanner;

public class Application {


    public static void main(String[] args) throws IOException {

        ItemService itemServices = new ItemServiceImplMap();
        Scanner teclado = new Scanner(System.in);
        String siteId, title, subTitle, categoryId, newTitle;


        System.out.println("Inserte los siguiente valores: ");
        System.out.print("Site Id: ");
        siteId = teclado.nextLine();
        System.out.print("Title: ");
        title = teclado.nextLine();
        System.out.print("SubTitle: ");
        subTitle = teclado.nextLine();
        System.out.print("Category Id: ");
        categoryId = teclado.nextLine();
        Item item = new Item(siteId, title, subTitle, 678786, categoryId,6786,"4", 4324,"nuevo",true, "reventa", "09/05/2018","09/05/2018");
        item = itemServices.insertItem(item);
        if (item == null){
            System.out.println("El item es no valido, site o category no corresponden");
            System.exit(1);
        }
        System.out.println("Insertar Item --> " + item);

        System.out.print("Cambiar el titulo: ");
        newTitle = teclado.nextLine();
        item.setTitle(newTitle);
        itemServices.updateItemById(item.getId(), item);
        System.out.println("Item actualizado  --> " + item);

        System.out.println("Obteniendo Item...");
        Item itemFromDB = itemServices.getItemById(item.getId());
        System.out.println("Item de la BD  --> " + itemFromDB);

        System.out.println("Eliminando Item...");
        itemServices.deleteItemById(item.getId());
        System.out.println("Item Eliminado");



    }
}