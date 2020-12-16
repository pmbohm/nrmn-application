package au.org.aodn.nrmn.restapi.service;

import au.org.aodn.nrmn.restapi.model.db.Diver;
import au.org.aodn.nrmn.restapi.model.db.Location;
import au.org.aodn.nrmn.restapi.model.db.Site;
import au.org.aodn.nrmn.restapi.repository.DiverRepository;
import au.org.aodn.nrmn.restapi.repository.SiteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Example;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CsvServiceTest {
    @InjectMocks
    CsvService csvService;

    @Mock
    DiverRepository diverRepository;
    @Mock
    SiteRepository siteRepository;

    @BeforeEach
    void init() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void getDiversCsv() throws IOException {
        when(diverRepository.findAll()).thenReturn(
                Arrays.asList(
                        Diver.builder().initials("GWB").fullName("George Bush").build(),
                        Diver.builder().initials("BHO").fullName("Barrack Obama").build(),
                        Diver.builder().initials("DJT").fullName("Donald Trump").build(),
                        Diver.builder().initials("101").fullName("Robopresident").build()
                ));

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        csvService.getDiversCsv(printWriter);
        List<String> csvLines = Arrays.stream(stringWriter.toString().split("\n"))
                .map(String::trim).collect(Collectors.toList());

        assertEquals(4, csvLines.size());
        assertEquals("INITIALS,FULL NAME", csvLines.get(0));
        assertEquals("BHO,Barrack Obama", csvLines.get(1));
        assertEquals("DJT,Donald Trump", csvLines.get(2));
        assertEquals("GWB,George Bush", csvLines.get(3));
    }

    @Test
    void getSitesCsv() throws IOException {
        Site.SiteBuilder builder = Site.builder()
                .state("Tasmania")
                .latitude(-43.1)
                .longitude(147.1)
                .siteName("Springfield");
        Site testSite333 = builder
                .siteCode("TAS333")
                .location(Location.builder().locationId(333).locationName("Southish").build()).build();
        Site testSite334 = builder
                .siteCode("TAS334")
                .location(Location.builder().locationId(334).locationName("Southish").build()).build();
        Site testSite335 = builder
                .siteCode("TAS335")
                .location(Location.builder().locationId(335).locationName("Southish").build()).build();
        Site testSite336 = builder
                .siteCode("VIC336")
                .state("Victoria")
                .location(Location.builder().locationId(336).locationName("Southish").build()).build();

        when(siteRepository.findSiteCodesByProvince("Antipodes")).thenReturn(Arrays.asList("TAS333"));
        when(siteRepository.findAll(Example.of(Site.builder().siteCode("TAS333").build())))
                .thenReturn(Arrays.asList(testSite333));

        when(siteRepository.findAll(Example.of(Site.builder().siteCode("TAS334").build())))
                .thenReturn(Arrays.asList(testSite334));

        when(siteRepository.findAll(Example.of(Site.builder().location(Location.builder().locationId(335).build()).build())))
                .thenReturn(Arrays.asList(testSite335));

        when(siteRepository.findAll(Example.of(Site.builder().state("Victoria").build())))
                .thenReturn(Arrays.asList(testSite336));


        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        csvService.getSitesCsv(printWriter,
                Arrays.asList(335),Arrays.asList("Antipodes"), Arrays.asList("Victoria"),Arrays.asList("TAS334"));

        List<String> csvLines = Arrays.stream(stringWriter.toString().split("\n"))
                .map(String::trim).collect(Collectors.toList());

        assertEquals(5, csvLines.size());
        assertEquals("SITE,Site Name,Latitude,Longitude,Region", csvLines.get(0));
        assertEquals("TAS333,Springfield,-43.1,147.1,Southish", csvLines.get(1));
        assertEquals("TAS334,Springfield,-43.1,147.1,Southish", csvLines.get(2));
        assertEquals("TAS335,Springfield,-43.1,147.1,Southish", csvLines.get(3));
        assertEquals("VIC336,Springfield,-43.1,147.1,Southish", csvLines.get(4));
    }
}
