package insomnia.summary;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.util.Iterator;
import java.util.Map;

import org.ektorp.ViewResult.Row;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;

import insomnia.json.JsonParser;
import insomnia.json.JsonWriter;
import insomnia.summary.Summary.Builder.BuilderException;

public class CouchSummaryFactory implements ISummaryFactory
{
	private static CouchSummaryFactory INSTANCE = new CouchSummaryFactory();

	private CouchSummaryFactory()
	{
	}

	public static CouchSummaryFactory getInstance()
	{
		return INSTANCE;
	}

	@Override
	public Summary load(InputStream in)
	{
		JsonParser parser = new JsonParser();
		Summary summary = new Summary.Builder(null).build();
		try
		{
			summary.data = parser.readJsonStream(in);
		}
		catch(ParseException | IOException e)
		{
			e.printStackTrace();
			System.exit(1);
		}
		return summary;
	}

	@Override
	public void save(OutputStream out, ISummary summary)
	{
		try
		{
			JsonWriter.writeJson(out, summary.getData(), true);
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * @param documents is the document set of a CouchDB
	 */
	@Override
	public Summary generate(Iterable<? extends Object> documents)
	{
		Summary.Builder builder = new Summary.Builder(Summary.Builder.RootType.OBJECT);
		for(Object d : documents)
		{
			Row row = (Row) d;
			try
			{
				buildingObject(builder, row.getDocAsNode());
			}
			catch(BuilderException e)
			{
				e.printStackTrace();
				System.exit(1);
			}
		}

		return builder.build();
	}

	private void buildingObject(Summary.Builder builder, JsonNode node) throws BuilderException
	{
		Iterator<Map.Entry<String, JsonNode>> it = node.fields();
		while(it.hasNext())
		{
			Map.Entry<String, JsonNode> entry = it.next();
			String key = entry.getKey();
			JsonNode value = entry.getValue();

			if(value.getNodeType() == JsonNodeType.OBJECT)
			{
				builder.addObject(key);
				buildingObject(builder, key, value);
				builder.goBack();
			}
			else if(value.getNodeType() == JsonNodeType.ARRAY)
			{
				builder.addArray(key);
				buildingArray(builder, key, value);
				builder.goBack();
			}
			else
				builder.addKey(key);
		}
	}

	private void buildingObject(Summary.Builder builder, String key, JsonNode object) throws BuilderException
	{
		Iterator<Map.Entry<String, JsonNode>> it = object.fields();
		while(it.hasNext())
		{
			Map.Entry<String, JsonNode> entry = it.next();
			String newKey = entry.getKey();
			JsonNode newValue = entry.getValue();

			if(newValue.getNodeType() == JsonNodeType.OBJECT)
			{
				builder.addObject(newKey);
				buildingObject(builder, newKey,  newValue);
				builder.goBack();
			}
			else if(newValue.getNodeType() == JsonNodeType.ARRAY)
			{
				builder.addArray(newKey);
				buildingArray(builder, newKey,  newValue);
				builder.goBack();
			}
			else
				builder.addKey(newKey);
		}
	}

	private void buildingArray(Summary.Builder builder, String key, JsonNode array) throws BuilderException
	{
		for(JsonNode element : array)
		{
			if(element.getNodeType() == JsonNodeType.OBJECT)
			{
				builder.addObject();
				buildingObject(builder, element);
				builder.goBack();
			}
		}
	}
}
