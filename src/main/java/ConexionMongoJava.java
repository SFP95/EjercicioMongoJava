import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.conversions.Bson;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Updates.*;
import static com.mongodb.client.model.Accumulators.avg;
import static com.mongodb.client.model.Accumulators.sum;
import static com.mongodb.client.model.Sorts.*;
import static com.mongodb.client.model.Projections.*;
import static com.mongodb.client.model.Aggregates.*;

import java.io.*;
import java.util.*;
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

        //es importante importar lo métodos de las clases Protections: import static com.mongodb.client.model.Projections.*;
        System.out.println("\n*** UTILIZAR PROYECCIONES ***");
        MongoCursor<Document> docs2 = colection.find(eq("curso","1DAM")).sort(ascending("nombre")).projection(include("nombre","nota")).iterator();
        while (docs2.hasNext()){
            Document docu = docs2.next();
            System.out.println(" - Documento con proyectcion en : nombre y nota");
            System.out.println(docu.toJson());
        }
        docs2.close();

        //UTILIZAR AGREGACIONES:

        //es importante importar lo métodos de las clases Aggregates: import static com.mongodb.client.model.Aggregates.*;
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

        //FORMA 4
        MongoCursor<Document> docs6 =  colection.aggregate(Arrays.asList(group("$curso",sum("sumanota","$nota"),avg("medianota","$nota")),out("medianota"))).iterator();
        while (docs6.hasNext()){
            Document docu = docs6.next();
            System.out.println("- Forma 4: usando la visualización anterior, almacenando los datos recogidos en una coleción nueva usando 'out'");
            System.out.println(docu.toJson());
        }
        docs6.close();

        //ACTUALIZAR DOCUMENTOS:

        //es importante importar los métodos de las clases Updates: import static com.mongodb.client.model.Updates.*;
        System.out.println("\n*** ACTUALIZAR DOCUMENTOS ***");

        //FORMA 1
        System.out.println("- Forma 1: actualizamos a Ana su nota con un 5, usando  updateOne ");
        colection.updateOne(eq("nombre","Ana"),set("nota",5));

        //FORMA 2
        System.out.println("- Forma 2: actualizamos el curso 1DAM con una nota de 3 usando updateMany");
        UpdateResult updateResult = colection.updateMany(eq("curso","1DAM"),inc("nota",3));
        System.out.println("\n - Se han modificado: " +  updateResult.getModifiedCount());
        System.out.println(" - Se han seleccionado: " +  updateResult.getMatchedCount());
        System.out.println("\n  -- Actualizando todos los datos de una colección con la nota de  2 , usando la función exists(''_id'') --");
        UpdateResult updateResult2 = colection.updateMany(exists("_id"),inc("nota",2));
        System.out.println("\n - Se han modificado: " +  updateResult2.getModifiedCount());
        System.out.println("\n  -- Añadir un campo, usando la función 'se', no existe, no se crea. Añadimos a Marleni : 'poblacion = Toledo' --");
        UpdateResult updateResult3 = colection.updateOne(eq("nombre","Marleni"),set("poblacion","Toledo"));
        System.out.println("\n - Se han modificado: " +  updateResult3.getModifiedCount());
        System.out.println("\n  -- Eliminar un campo utilizando 'unset'. Borramos los campos de '1DAM' que tengan 'poblacion' --");
        UpdateResult updateResult4 = colection.updateOne(eq("nombre","1DAM"),unset("poblacion"));
        System.out.println("\n - Se han modificado: " +  updateResult4.getModifiedCount());

        //BORRAR UN DOCUMENTO DE LA COLECCION:
        System.out.println("\n*** BORRAR UN DOCUMENTO DE LA COLECCION ***");

        //Borramos un documento
        System.out.println(" -Borramos un documento que contenga 'nombre = Ana' ");
        DeleteResult del = colection.deleteOne(eq("nombre","Ana"));
        System.out.println("   Se ha borrado: "+del.getDeletedCount());

        //Borrar todos
        System.out.println(" -Borramos todos");
        del = colection.deleteMany(exists("_id"));
        System.out.println("   Se han borrado: "+del.getDeletedCount());

        //CREAR Y BORRAR UN COLECCION:
        System.out.println("\n*** CREAR Y BORRAR UN COLECCION ***");

        System.out.println(" -Creamos una nueva coleccion 'nuevaColeccion2' en 'mibasededatos', creando un nuevo cliente 'cliente2' primero");
        MongoClient client2 = new MongoClient();
        MongoDatabase db2 = client2.getDatabase("mibasededatos");
        db2.createCollection("nuevaColeccion2");
        MongoCollection<Document> cnueva = db2.getCollection("nuevaColeccion2");

        //La borramos
        System.out.println(" -Borraos la colección creada");
        cnueva.drop();

        //LISTAR LAS COLECCIONES DE LA BD
        System.out.println("\n*** LISTAR LAS COLECCIONES DE LA BD ***");

        System.out.println("  -Listado de colecciones:");
        MongoIterable<String> coleciones = db.listCollectionNames();
        Iterator col= coleciones.iterator();
        while (col.hasNext()){
            System.out.println("    FORMA 1:");
            System.out.println(col.next());
        }

        //Tambien se puede hacer de esta forma
        for (String name : db.listCollectionNames()){
            System.out.println("    FORMA 2:");
            System.out.println(name);
        }

        //CREAR, LISTAR Y BORRAR BD
        System.out.println("\n*** CREAR, LISTAR Y BORRAR BD ***");

        System.out.println(" -Creamos una BD = 'nueva'");
        MongoClient client3 = new MongoClient();
        MongoDatabase db3 = client3.getDatabase("nueva");
        MongoCollection<Document> clnueva = db3.getCollection("coleccionNueva");
        Document document = new Document("nombre","Pedro").append("telefono",1234).append("curso","2DAM");

        System.out.println(" -Listamos los nombres de la BD");
        for (String name: client3.listDatabaseNames()){
            System.out.println(name);
        }

        System.out.println(" -Borramos la BD 'nueva' ");
        client3.getDatabase("nueva").drop();

        //PASAR DATOS DE MONGO A UN FICHERO DE TEXTO
        System.out.println("\n*** PASAR DATOS DE MONGO A UN FICHERO DE TEXTO ***");

        System.out.println(" -Crear una fichero con los datos de una coleccion:");
        crearFichroJson();

        System.out.println(" -Leer date de un fichero en Json, almacenandolo en una coleccion:");
        leerFicheroyGuardarDatos();
    }

    private static void leerFicheroyGuardarDatos () {
        try {
            FileReader fr = new FileReader("./amigos.json");
            BufferedReader bf = new BufferedReader(fr);
            MongoClient client = new MongoClient();
            MongoDatabase db = client.getDatabase("mibasodedatos");
            MongoCollection<Document> collection = db.getCollection("amigosfile");
            String cadenaJson;

            while ((cadenaJson = bf.readLine()) != null){
                System.out.println(cadenaJson);
                Document docu = new Document(org.bson.Document.parse(cadenaJson));
                collection.insertOne(docu);
            }
        }catch (FileNotFoundException e){
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    private static void crearFichroJson () {
        System.out.println("**Creamos objetos: cliente, db, coleccion, fichero (amigos.json) y fic **");
        MongoClient client = new MongoClient();
        MongoDatabase db = client.getDatabase("mibasededatos");
        MongoCollection<Document> collection = db.getCollection("amigos");
        File fiche = new File("./amigos.json");
        FileWriter fic;

        try {
            fic = new FileWriter(fiche);
            BufferedReader ficheroBF = new BufferedReader(fic);
            System.out.println("\n- Recorremos la colecion");
            List<Document> consulta = collection.find().into(new ArrayList<Document>());

            for (int i=0; i<consulta.size();i++){
                System.out.println(" Grados elemento "+i+", "+consulta.get(i).toString());
                ficheroBF.write(consulta.get(i).toJson());
                ficheroBF.newLine();
            }
            ficheroBF.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
