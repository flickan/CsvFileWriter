package main;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import de.lsb.masterdata.domainmodel.geographic.entity.Airport;
import de.lsb.masterdata.domainmodel.geographic.home.AirportHome;
import de.lsb.masterdata.services.geographic.converter.AirportToConverter;
import de.lsb.masterdata.services.geographic.transferobject.AirportTO;
import de.lsb.persistenceinfrastructure.domainmodel.HomeLocator;
import de.lsb.util.prefs.GlobalPreferences;
import de.lsb.utilityinfrastructure.exception.ExtendedException;
import de.lsb.utilityinfrastructure.file.csv.CsvWriter;
import de.lsb.utilityinfrastructure.junit.marker.Persistence;

public class DataLoader {

	public static void main(String[] args) throws FileNotFoundException,
			IOException {

		configureLogger();
		writeAirpotsInFile();
	}

	/**
	 * Configures logger needed in {@link Persistence}
	 */
	private static void configureLogger() {
		BasicConfigurator.configure(); // basic log4j configuration
		Logger.getRootLogger().setLevel(Level.INFO);
	}

	/**
	 * Load data from sta_airport and saves it in file "output.csv".
	 * 
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private static void writeAirpotsInFile() throws FileNotFoundException,
			IOException {

		GlobalPreferences.importProperties(new FileInputStream(
				"parameter/parameter.txt"));

		try {
			final AirportHome home = HomeLocator.getHome(AirportHome.class);
			final List<Airport> airportEntities = home.findAll();

			String csv = "output.csv";
			CsvWriter writer = new CsvWriter(new FileWriter(csv));
			// write column names into file
			writer.write("IATA_AP_CODE,AP_NAME,IATA_CITY_CODE,CITY_NAME,ISO_COUNTRY_CODE,COUNTRY_NAME,AP_STANDARD_TIME_VARIATION"
					.split(","));

			for (final Airport airportEntity : airportEntities) {
				final AirportTO airportTO = AirportToConverter
						.getInstance(true).convert(airportEntity);

				final List<String> list = new ArrayList<String>();

				// iata airport code
				list.add(airportTO.getIataCode().getIataCode());
				// airport name
				list.add(airportTO.getName());
				// iata city code
				list.add(airportTO.getSuperiorGeographicTO().getIataCode()
						.getIataCode());
				// city name
				list.add(airportTO.getSuperiorGeographicTO().getName());
				// iso country code
				list.add(airportTO.getSuperiorGeographicTO()
						.getSuperiorGeographicTO().getIsoCodeTO().getCode());
				// country code
				list.add(airportTO.getSuperiorGeographicTO()
						.getSuperiorGeographicTO().getName());
				// standard time variation
				list.add(airportEntity.getStandardTimeVariation().toString());

				writer.write(list);
			}

			writer.close();
		} catch (final ExtendedException e) {
			throw new RuntimeException(e);
		}

	}
}
