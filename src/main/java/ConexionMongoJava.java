import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Filter;

public class ConexionMongoJava {
    public static void main(String[] args) {

        //establecemos conexión a mongo, instalando primero las dependencias necesarias en el pom.xml
         MongoClient cliente = new MongoClient();
        //seleccionamos la database en la que quermso trabajar
         MongoDatabase db= cliente.getDatabase("mibasodedatos");
        //selecionamos la coleccion
         MongoCollection<Document> colection= db.getCollection("amigos");

        //VISUALIZACIONES:

        //-Creamos una lista con el contenido de la collección de mongo
         List<Document> consulta= colection.find().into(new ArrayList<Document>());

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

        // creamos un nuevo objeto documento
        Document amigo= new Document();
        System.out.println("\n*** REALIZAMOS INSERCIONES DE UN NUEVO DOCUMENTO A LA COLECION ***");

        System.out.println("-FORMA 1 DE INSERCION:");
        //insertamos registros
        amigo.put("nombre","Pepito2");
        amigo.put("telefono",555);
        amigo.put("curso","2DAM2");
        amigo.put("fecha",new Date());

        //subimos todos los registros a la colecion
        colection.insertOne(amigo);

        System.out.println("-FORMA 2 DE INSERCION:");
        //Otra forma de hacerlo sería usando el .append para encadedar inserciones en una sola línea de código
        Document amigo2= new Document("nombre","Pedro")
                .append("telefono",1234)
                .append("curso",new Document("curso1","1DAM")
                        .append("curso2","2DAM"));
        colection.insertOne(amigo2);

        System.out.println("-FORMA DE INSERTAR UN COLECCION:");

        //Insertamos una colección con insertMany:

        //Hacems un nuesvo listado de tipo array
        List<Document> listadocs = new ArrayList<Document>();

        //recorremos el nuevo listado
        for (int i = 0; i < 100; i++){
            listadocs.add(new Document("Valor de i",i));
        }
        colection.insertMany(listadocs);

        //CONSUTAR DOCUMENTOS: usando cursores he iteratorspara recorrer los primeros
        MongoCursor<Document> cursor= colection.find().iterator();
        while(cursor.hasNext()){
            Document doc = cursor.next();
            System.out.println(doc.toJson());
        }
        cursor.close();

        //FILTROS EN COLSULTAS:

        //generamos un documento usando el comando Filters.eq para filtar la información
        // y mosntrándo solo el primer documento que conhincida

        Document doc = colection.find(Filters.eq("nombre","Ana")).first();
        try{
            System.out.println("Localizado: "+doc.toJson());
        }catch (NullPointerException e){
            System.out.println("No se encuentra. Error en "+ e);
        }
    }
}
