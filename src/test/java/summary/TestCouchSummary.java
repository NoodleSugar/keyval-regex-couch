package summary;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import org.ektorp.CouchDbConnector;
import org.ektorp.CouchDbInstance;
import org.ektorp.ViewQuery;
import org.ektorp.ViewResult;
import org.ektorp.http.HttpClient;
import org.ektorp.http.StdHttpClient;
import org.ektorp.impl.StdCouchDbConnector;
import org.ektorp.impl.StdCouchDbInstance;
import org.junit.jupiter.api.Test;

import insomnia.query.CouchQueryFactory;
import insomnia.regex.RegexParser;
import insomnia.regex.automaton.RegexAutomaton;
import insomnia.regex.automaton.RegexToAutomatonConverter;
import insomnia.regex.element.IElement;
import insomnia.summary.CouchSummaryFactory;
import insomnia.summary.Summary;

@SuppressWarnings("unused")
class TestCouchSummary
{

	@Test
	void test() throws Exception
	{
		String regex = "a.b+.c?";
		// Automate
		RegexParser parser = new RegexParser();
		IElement elements = parser.readRegexStream(new ByteArrayInputStream(regex.getBytes()));
		RegexAutomaton automaton = RegexToAutomatonConverter.convert(elements);

		// R�sum�
		CouchSummaryFactory factory = CouchSummaryFactory.getInstance();
		Summary summary;

		// CouchDB
		HttpClient httpClient = new StdHttpClient.Builder().url("http://localhost:5984").build();
		CouchDbInstance dbInstance = new StdCouchDbInstance(httpClient);
		CouchDbConnector db = new StdCouchDbConnector("test", dbInstance);
		db.createDatabaseIfNotExists();
		ViewQuery q = new ViewQuery().allDocs().includeDocs(true);
		ViewResult view = db.queryView(q);

		// Generation du r�sum�
		//summary = factory.generate(view.getRows());

		// Enregistrement du r�sum�
		//OutputStream out = new FileOutputStream("src/test/ressources/summary/testsummary.json");
		//factory.save(out, summary);
		//out.close();

		// Chargement du r�sum�
		InputStream in = new
		FileInputStream("src/test/ressources/summary/TestSummary.json");
		summary = factory.load(in);
		in.close();

		ArrayList<String> paths = automaton.getPathsFromSummary(summary);
		System.out.println("Regex: " + regex);
		System.out.println("Valid paths:");
		for(String s : paths)
			System.out.println(s);

		System.out.println("\nCouchDB queries:");
		ArrayList<String> queries = CouchQueryFactory.getInstance().getQueries(paths, summary);
		for(String s : queries)
			System.out.println(s);
	}

}
