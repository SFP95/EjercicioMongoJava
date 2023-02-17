import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.conversions.Bson;

import static com.mongodb.client.model.Accumulators.avg;
import static com.mongodb.client.model.Accumulators.sum;
import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Sorts.*;
import static com.mongodb.client.model.Projections.*;
import static com.mongodb.client.model.Aggregates.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.logging.Filter;

import static com.mongodb.client.model.Filters.eq;

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
            System.out.println("\n** Consulta simple **");
            System.out.println("-"+ consulta.get(i).toString());
        }

        //-recogida de consulta de datos usando parametros: nombre, teléfono y nota

        for( int i=0; i < consulta.size(); i++){
            Document amig= consulta.get(i);
            System.out.println("\n** Consulta con parametros [ nombre, telefono y nota ] **");
            System.out.println(" - "+amig.getString("nombre")+
                    " - "+amig.getString("telefono")+
                    " - "+ amig.get("nota"));
        }

        //INSERCIONES:

        // creamos un nuevo objeto documento
        Document amigo= new Document();
        System.out.println("\n*** REALIZAMOS INSERCIONES DE UN NUEVO DOCUMENTO A LA COLECION ***");

        System.out.println("-FORMA 1 DE INSERCION DE REGISTRO EN COLECCIONES:");
        //insertamos registros
        amigo.put("nombre","Pepito2");
        amigo.put("telefono",555);
        amigo.put("curso","2DAM2");
        amigo.put("fecha",new Date());

        //subimos todos los registros a la colecion
        colection.insertOne(amigo);

        System.out.println("-FORMA 2 DE INSERCION DE REGISTRO EN COLECCIONES:");
        //Otra forma de hacerlo sería usando el .append para encadedar inserciones en una sola línea de código
        Document amigo2= new Document("nombre","Pedro")
                .append("telefono",1234)
                .append("curso",new Document("curso1","1DAM")
                        .append("curso2","2DAM"));
        colection.insertOne(amigo2);

        //Insertamos una colección con insertMany:
        System.out.println("-FORMA DE INSERTAR UN DOCUMENTO:");

        //Hacems un nuesvo listado de tipo array
        List<Document> listadocs = new ArrayList<Document>();

        //recorremos el nuevo listado
        for (int i = 0; i < 100; i++){
            listadocs.add(new Document("Valor de i",i));
        }
        colection.insertMany(listadocs);

        //CONSUTAR DOCUMENTOS: usando cursores he iteratorspara recorrer los primeros
        System.out.println("\n*** CONSULTA CON CURSOR + ITERATOR ***");

        MongoCursor<Document> cursor= colection.find().iterator();
        while(cursor.hasNext()){
            Document doc = cursor.next();
            System.out.println(doc.toJson());
        }
        cursor.close();

        //CONSULTAR DOCUMENTOS:
        System.out.println("\n*** CONSULTAS CON FILTROS USANDO CURSORES ***");

        MongoCursor<Document> cursor2 = colection.find().iterator();
        while (cursor2.hasNext()){
            Document doc2 = cursor2.next();
            System.out.println("- Forma : usando find() + iterator() para visualizar en formato Json");
            System.out.println(doc2.toJson());
        }
        cursor2.close();

        //USAR FILTROS EN COLSUTAS:

        //Forma 1
        System.out.println("\n*** UTILIZAR FILTRO EN LAS CONSULTAS ***");
        Document doc = colection.find(eq("nombre","Ana")).first();
        try{
            System.out.println("- Forma 1: Filtamos por nombre = Ana y solo queremos el primer resultado ");
            System.out.println("Localizado "+doc.toJson());
        }catch (NullPointerException e){
        System.out.println("No se encuentra. Error en "+ e);
        }

        //Forma 2
        MongoCursor<Document> doc2 = colection.find(eq("curso","2DAM")).iterator();
        while ( doc2.hasNext()){
            Document docu = doc2.next();
            System.out.println("- Forma 2: Cuando el filtro devuelve varios documentos, recuperamos con un cursor o lista");
            System.out.println(docu.toJson());
        }
        doc2.close();

        //Forma 3
        System.out.println(" - Forma 3: Cuando deseamos extraer objetos BSON de un documento usando filtros");
        System.out.println("\n ---- Objetos Bson ----");
        MongoCursor<Document> cursor3 = colection.find().iterator();
        while (cursor3.hasNext()){
            Document doc3 = cursor3.next();
            Bson id = eq("id",doc3.get("_id"));
            Bson nombre = eq("nombre",doc3.get("nombre"));
            Bson curso = eq("curso",doc3.get("curso"));
            System.out.println(" -Id: "+id+"\n -Nombre: "+nombre+"\n -Curso: "+curso.toString());
        }

        //ORDENAR RESULTADOS:

        //es importante importar lo métodos de las clases Sort: import static com.mongodb.client.model.Sorts.*;
        System.out.println("\n*** ORDENAR RSULTADOS ***");
        MongoCursor<Document> docs = colection.find(eq("curso","2DAM")).sort(descending("nombre")).iterator();
        while (docs.hasNext()){
            Document docu = docs.next();
            System.out.println( docu.toJson());
        }
        docs.close();

        //ordenamos la salida usando la lista
        System.out.println("- Ordenado de la lista anterior:");
        List<Document> cosulta = colection.find(and(eq("curso","2DAM"),eq("nota",8))).into(new ArrayList<Document>());
        for (int i=0; i<consulta.size();i++){
            System.out.println(" -- "+cosulta.get(i).toString());
        }

        //UTILIZAR PROYECCIONES:

        //es importante importar lo métodos de las clases Sort: import static com.mongodb.client.model.Projections.*;
        System.out.println("\n*** UTILIZAR PROYECCIONES ***");
        MongoCursor<Document> docs2 = colection.find(eq("curso","1DAM")).sort(ascending("nombre")).projection(include("nombre","nota")).iterator();
        while (docs2.hasNext()){
            Document docu = docs2.next();
            System.out.println(" - Documento con proyectcion en : nombre y nota");
            System.out.println(docu.toJson());
        }
        docs2.close();

        //UTILIZAR AGREGACIONES:

        //es importante importar lo métodos de las clases Sort: import static com.mongodb.client.model.Aggregates.*;
        System.out.println("\n*** UTILIZAR AGREGACIONES ***");

        //FORMA 1
        MongoCursor<Document> docs3 = colection.aggregate(Arrays.asList(match(eq("curso","1DAM")))).iterator();
        while (docs3.hasNext()){
            Document docu = docs3.next();
            System.out.println("- Forma 1: visualizando los amigos del curso 1DAM");
            System.out.println(docu.toJson());
        }
        docs3.close();

        //FORMA 2
        MongoCursor<Document> docs4 = colection.aggregate(Arrays.asList(match(eq("curso","1DAM")), project(fields(include("nombre","nota"), exclude())))).iterator();
        while (docs4.hasNext()){
            Document docu = docs4.next();
            System.out.println("- Forma 2: visualizando  nombre y nota usando project");
            System.out.println(docu.toJson());
        }
        docs4.close();

        //FORMA 3
        MongoCursor<Document> docs5 =  colection.aggregate(Arrays.asList(group("$curso",sum("sumanota","$nota"),avg("medianota","$nota")))).iterator();
        while (docs5.hasNext()){
            Document docu = docs5.next();
            System.out.println("- Forma 3: visualizando  curso + suma y media de las notas usando sum y avg");
            System.out.println(docu.toJson());
        }
        docs5.close();
    }
}
