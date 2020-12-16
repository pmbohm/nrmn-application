package au.org.aodn.nrmn.restapi.service;

import au.org.aodn.nrmn.restapi.model.db.Diver;
import au.org.aodn.nrmn.restapi.model.db.Location;
import au.org.aodn.nrmn.restapi.model.db.Site;
import au.org.aodn.nrmn.restapi.repository.DiverRepository;
import au.org.aodn.nrmn.restapi.repository.SiteRepository;
import cyclops.companion.Streams;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

@Service
public class CsvService {
    private static final CSVFormat DIVERS_FORMAT = CSVFormat.DEFAULT.withHeader("INITIALS", "FULL NAME");
    private static final CSVFormat SITES_FORMAT = CSVFormat.DEFAULT.withHeader("SITE", "Site Name", "Latitude", "Longitude", "Region");

    @Autowired
    DiverRepository diverRepository;

    @Autowired
    SiteRepository siteRepository;


    public void getDiversCsv(PrintWriter writer) throws IOException {
        CSVPrinter csvPrinter = DIVERS_FORMAT.print(writer);
        List<Diver> divers = diverRepository.findAll();
        List<List<String>> records = divers.stream()
                .filter(d -> d.getInitials().matches("[A-Z]*"))
                .map(this::getDiverAsCsvRecord).collect(toList());
        csvPrinter.printRecords(records);
    }

    private List<String> getDiverAsCsvRecord(Diver diver) {
        return Arrays.asList(diver.getInitials(), diver.getFullName());
    }

    public void getSitesCsv(PrintWriter writer,
                            Collection<Integer> locations,
                            Collection<String> provinces,
                            Collection<String> states,
                            Collection<String> siteCodes) throws IOException {
        CSVPrinter csvPrinter = SITES_FORMAT.print(writer);

        Stream<String> siteCodesFromProvinces = provinces == null ? Stream.empty() : provinces.stream()
                .distinct()
                .flatMap(p -> siteRepository.findSiteCodesByProvince(p).stream());

        Stream<Site> sites = (siteCodes == null
                ? siteCodesFromProvinces
                : Streams.concat(siteCodesFromProvinces, siteCodes.stream()))
                .distinct()
                .flatMap(sc -> siteRepository.findAll(Example.of(Site.builder().siteCode(sc).build())).stream());


        if(locations != null) {
            sites = Stream.concat(sites, locations.stream()
                    .distinct()
                    .flatMap(l -> siteRepository.findAll(
                            Example.of(Site.builder()
                                    .location(Location.builder().locationId(l).build()).build())).stream()));
        }

        if(states != null) {
            sites = Stream.concat(sites, states.stream()
                    .distinct()
                    .flatMap(s -> siteRepository.findAll(Example.of(Site.builder().state(s).build())).stream()));
        }

        List<List<String>> records = sites
                .distinct()
                .sorted(Comparator.comparing(Site::getSiteCode))
                .map(this::getSiteAsCsvRecord)
                .collect(toList());


        csvPrinter.printRecords(records);
    }

    private List<String> getSiteAsCsvRecord(Site site) {
        return Arrays.asList(
                site.getSiteCode(),
                site.getSiteName(),
                toString(site.getLatitude()),
                toString(site.getLongitude()),
                site.getLocation().getLocationName());
    }

    private String toString(Object couldBeNull) {
        return couldBeNull == null ? null : couldBeNull.toString();
    }
}
