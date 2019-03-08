import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.FileManager;
import org.apache.jena.vocabulary.RDF;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.*;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDFS;

/*Questo file, attraverso delle interrogazioni ARQ di DBpedia,
 * permette di linkare degli individui (in questo caso dei pianeti e lune)
 * presenti nell'ontologia agli individui registrati su DBpedia mediante
 * la proprietà owl:sameAs.
 */

public class reasonerDestiny extends Base{
  private static String fnameschema = "destiny";
  private static String NS = "http://www.semanticweb.org/gianluca/ontologies/2018/11/destiny#";
  private static String DBpedia = "http://dbpedia.org/resource/";

  
  
  public static void main(String args[]) {
	 
	  
	  new reasonerDestiny().run();
  }
  public void run() {
	  
	  Model schema = FileManager.get().loadModel(fnameschema);
      String prefix = "prefix destiny: <" + NS + ">\n" +
                      "prefix rdfs: <" + RDFS.getURI() + ">\n" +
                      "prefix owl: <" + OWL.getURI() + ">\n" +
                      "prefix yago: <http://dbpedia.org/class/yago/>\n" +
                      "prefix dbo: <http://dbpedia.org/ontology/>\n" +
                      "prefix foaf: <http://xmlns.com/foaf/0.1/>" +
                      "prefix bif: <bif:>";

      
      //query alla mia ontologia
      List<String> ont_ans = showOntologyQuery( schema,
                 prefix + "select ?planet where{ ?planet a destiny:Planet}");
      
      //query a dbpedia
      List<String> db_ans = showServiceQuery(prefix + 
    		"SELECT DISTINCT ?planet "+
      		"WHERE {?planet a yago:WikicatPlanetsOfTheSolarSystem}");
       
      for(int i=0;i<ont_ans.size();i++) {
    	  for(int j=0;j<db_ans.size();j++) {
    		  if(db_ans.get(j).contains(ont_ans.get(i))) {
    			  Resource res = schema.getResource(NS+ont_ans.get(i));
    			  res.addProperty(OWL.sameAs, DBpedia+db_ans.get(j));
    			  System.out.println(NS+ont_ans.get(i)+ "\t|| owl:sameAs || " + DBpedia+db_ans.get(j));
    		  }
    	  }
      }     
      
      //quali sono i luoghi luna e astoroide
      ont_ans = showOntologyQuery(schema,
    		  prefix + "select ?place where{{?place a destiny:Moon}"
    		  + "UNION {?place a destiny:Asteroid}}");
      
    //query a dbpedia per i luoghi non pianeti che matchano i non-pianeti dell'ontologia
      for(int i=0; i<ont_ans.size();i++) {
    	  db_ans = showServiceQuery(prefix + "select ?Concept where {?Concept a dbo:CelestialBody . "+
        	    		  "?Concept foaf:name ?name."
        	    		  + "?name bif:contains \""+ont_ans.get(i)+"\"}");
      
    	 //matching sugli individui di tipo moon
    	 Resource temp = schema.getResource(NS+ont_ans.get(i));
    	 if(temp.hasProperty(RDF.type, schema.getResource(NS+"Moon"))) {
    		 for(int k =0;k<db_ans.size();k++) {
    			 if(db_ans.get(k).contains("moon")) {
    				temp.addProperty(OWL.sameAs, DBpedia+db_ans.get(k));
       			  	System.out.println(temp.toString()+ "\t|| owl:sameAs || " + DBpedia+db_ans.get(k));

    			 }
    		 }
    	 }
    	 //matching sugli individui di tipo asteroid
    	 else {
    		 for(int k =0;k<db_ans.size();k++) {
	    		 if(db_ans.get(k).contains(ont_ans.get(i))) {
	    			 Resource res = schema.getResource(NS+ont_ans.get(i));
	    			 res.addProperty(OWL.sameAs, DBpedia+db_ans.get(k));
	    			 System.out.println(res.toString()+ "\t|| owl:sameAs || " + DBpedia+db_ans.get(k));

	    		}
    		 }
    	 }
      }
      
      try {
          schema.write(new FileOutputStream("destiny_extended.rdf"), "RDF/XML-ABBREV");
      } catch (Exception e) {
    	  System.out.println(e);
      }
}
      
  protected List<String> showOntologyQuery( Model m, String q ) {
	  List<String> answer = new ArrayList<String>();      
      Query query = QueryFactory.create( q );
      QueryExecution qexec = QueryExecutionFactory.create( query, m );
      try {
    	  ResultSetRewindable result = ResultSetFactory.makeRewindable( qexec.execSelect() );
		  //System.out.println(ResultSetFormatter.asText(result));
		  result.reset(); // back to the start
		  while (result.hasNext()) {
			  QuerySolution temp = result.next();
			  String res = temp.toString().substring(temp.toString().indexOf("<")+1, temp.toString().indexOf(">"));
			  res = res.replaceAll(NS, "");
			  answer.add(res);
		  }
      }
      finally {
          qexec.close();
      }
      return answer;

  }
  protected List<String> showServiceQuery(String q ) {
	  List<String> answer = new ArrayList<String>();      
	  Query query = QueryFactory.create( q );
      QueryExecution qexec = QueryExecutionFactory.sparqlService("http://dbpedia.org/sparql", query);
      try {
    	  ResultSetRewindable result = ResultSetFactory.makeRewindable( qexec.execSelect() );
		  //System.out.println(ResultSetFormatter.asText(result));
		  result.reset(); // back to the start
		  while (result.hasNext()) {
			  QuerySolution temp = result.next();
			  String res = temp.toString().substring(temp.toString().indexOf("<")+1, temp.toString().indexOf(">"));
			  res = res.replaceAll(DBpedia, "");
			  answer.add(res);
		  }
      }
      finally {
          qexec.close();
      }
      return answer;

  }
}