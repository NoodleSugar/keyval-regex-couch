package insomnia.query;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import insomnia.summary.ISummary;

public final class CouchQueryFactory implements IQueryFactory
{
	private static CouchQueryFactory INSTANCE = null;

	private CouchQueryFactory()
	{
	}

	public static CouchQueryFactory getInstance()
	{
		if(INSTANCE == null)
			INSTANCE = new CouchQueryFactory();
		return INSTANCE;
	}

	@SuppressWarnings("unchecked")
	@Override
	public ArrayList<String> getQueries(List<String> paths, ISummary summary)
	{
		ArrayList<String> queries = new ArrayList<String>();
		StringBuffer query = new StringBuffer();

		for(String path : paths)
		{
			String[] splitted = path.split("\\.");
			int n = splitted.length - 1;
			Object data = summary.getData();

			query.append("{\"selector\":{");
			int bracketNumber = 2;
			for(int i = 0; i < n; i++)
			{
				String word = splitted[i];

				query.append("\"").append(word).append("\":{");
				bracketNumber++;

				if(data instanceof List)
					data = ((List<Object>) data).get(0);

				if(data instanceof Map)
					data = ((Map<String, Object>) data).get(word);
				else
					throw new InvalidParameterException("Paths and Summary seem to be inconsistent");

				if(data instanceof List)
				{
					query.append("\"$elemMatch\":{");
					bracketNumber++;
				}
			}
			query.append("\"").append(splitted[n]).append("\":{\"$exists\" : true}");
			for(int i = 0; i < bracketNumber; i++)
				query.append("}");
			queries.add(query.toString());
			query.setLength(0);
		}
		return queries;
	}
}
