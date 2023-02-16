import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.sql.Array;
import java.util.ArrayList;
import java.util.List;

public class ConexionMongoJava {

    //establecemos conexión a mongo, instalando primero las dependencias necesarias en el pom.xml
    MongoClient cliente = new MongoCliente();
    //seleccionamos la database en la que quermso trabajar
    MongoDatabase db= cliente.getDatabase("mibasodedatos");
    //selecionamos la coleccion
    MongoCollection<Document> collection= db.getCollection("amigos");

    //VISUALIZACIONES:

    //-Creamos una lista con el contenido de la collección de mongo
    List<Document> consulta= collection.find().into(new ArrayList<Document>());

    //-recorremos la consulta imprimiendo por pantalla los resultados optenidos
    for( int i=0; i < consulta.size(); i++){
        System.out.println("** Consulta simple **");
        System.out.println("-"+ consulta.get(i).toString());
    }

    //-recogida de consulta de datos usando parametros: nombre, teléfono y nota

    for( int i=0; i < consulta.size(); i++){
        Document amig= consulta.get(i);
        System.out.println("** Consulta con parametros [ nombre, telefono y nota ] **");
        System.out.println(" - "+amig.getString("nombre")+
                " - "+amig.getString("telefono")+
                " - "+ amig.get("nota"));
    }

    //INSERCIONES:

}
