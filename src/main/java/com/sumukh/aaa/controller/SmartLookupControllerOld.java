//package com.sumukh.aaa.controller;
//
//import com.sumukh.aaa.model.*;
//import com.sumukh.aaa.dto.*;
//import com.sumukh.aaa.repository.*;
//import com.sumukh.aaa.service.AviationService;
//import jakarta.persistence.EntityNotFoundException;
//import jakarta.transaction.Transactional;
//import lombok.RequiredArgsConstructor;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.time.ZoneId;
//import java.time.ZonedDateTime;
//import java.time.format.DateTimeFormatter;
//import java.util.*;
//import java.util.regex.Pattern;
//
//@RestController
//@RequestMapping("/")
//@RequiredArgsConstructor
//public class SmartLookupController {
//
//  private final FlightRepository flightRepo;
//  private final AirportRepository airportRepo;
//  private final AirlineRepository airlineRepo;
//  private final TailNumberRepository tailRepo;
//  private final RunwayRepository runwayRepo;
//
//  @Autowired
//  AviationService svc;
//
//  // Patterns (case-insensitive)
//  private static final Pattern IATA3 = Pattern.compile("^[A-Z]{3}$", Pattern.CASE_INSENSITIVE);
//
//  // Tail: common Indian style "VT-XXX" or "VTXXX", but also allow AA-ABC, AAABC (2-3 letters + optional hyphen + 2-5 letters/digits)
//  private static final Pattern TAIL = Pattern.compile("^[A-Z]{2,3}-?[A-Z0-9]{2,5}$", Pattern.CASE_INSENSITIVE);
//
//  // Flight: 2 alnum airline code + optional hyphen + 1–4 digits (allow leading zeros)
//  private static final Pattern FLIGHT = Pattern.compile("^[A-Z0-9]{2}-?\\d{1,4}$", Pattern.CASE_INSENSITIVE);
//
//  private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
//
////  @GetMapping("{code}")
////  public Object universalLookup(@PathVariable String code) {
////    String raw = code.trim();
////
////    // Try flight first (matches your /6E-123, /6e0123 etc)
////    if (FLIGHT.matcher(raw).matches()) {
////      return lookupFlight(raw);
////    }
////
////    // Then tail (matches /VT-ILO, /vtilo, etc)
////    if (TAIL.matcher(raw).matches()) {
////      return lookupTail(raw);
////    }
////
////    // Then 3-letter IATA (matches /blr or /BLR)
////    if (IATA3.matcher(raw).matches()) {
////      return lookupAirport(raw);
////    }
////
////    throw new IllegalArgumentException("Unsupported code format: " + raw);
////  }
//
//  @GetMapping("/{flight:^(?i)[a-z0-9]{2}-?\\d{1,4}$}")
//  public Object flight(@PathVariable String flight) {
//    return lookupFlight(flight);
//  }
//
//  // TAIL: 2–3 letters + optional hyphen + 2–5 alnum
//  @GetMapping("/{tail:^(?i)[a-z]{2,3}-?[a-z0-9]{2,5}$}")
//  public Object tail(@PathVariable String tail) {
//    return lookupTail(tail);
//  }
//
//  // IATA: 3 letters
//  @GetMapping("/{iata:^(?i)[a-z]{3}$}")
//  public Object airport(@PathVariable String iata) {
//    return lookupAirport(iata);
//  }
//
//  // ---------- helpers ----------
//
//  private FlightLookupResponse lookupFlight(String code) {
//    String normalized = normalizeFlight(code);        // e.g., "6E-0123" -> "6E0123"
//    String alt = normalizedWithHyphen(normalized);    // "6E0123" -> "6E-0123"
//
//    Optional<Flight> opt = flightRepo.findByFlightNumber(normalized);
//    if (opt.isEmpty()) opt = flightRepo.findByFlightNumber(alt);
//
//    Flight f = opt.orElseThrow(() -> new EntityNotFoundException("Flight not found: " + code));
//
//    Airline airline = f.getAirline();
//    TailNumber tail = f.getTailNumber();
//    Aircraft ac = (tail != null) ? tail.getAircraftType() : null;
//
//    return new FlightLookupResponse(
//      airline != null ? airline.getName() : null,
//      f.getOrigin() != null ? f.getOrigin().getIataCode() : null,
//      f.getDestination() != null ? f.getDestination().getIataCode() : null,
//      f.getScheduledDeparture() != null ? ISO.format(f.getScheduledDeparture()) : null,
//      f.getScheduledArrival() != null ? ISO.format(f.getScheduledArrival()) : null,
//      tail != null ? tail.getTailNumber() : null,
//      ac != null ? ac.getModel() : null,
//      ac != null ? ac.getBrand() : null,
//      ac != null ? ac.getSeatLayoutJson() : null,
//      ac != null ? ac.getPaxNumber() : null,
//      ac != null ? ac.getRangeKm() : null
//    );
//  }
//
//  private TailLookupResponse lookupTail(String code) {
//    String normalized = normalizeTail(code); // e.g., "vt-ilo" -> "VT-ILO" and also check "VTILO"
//    String alt = normalized.replace("-", "");
//
//    Optional<TailNumber> opt = tailRepo.findByTailNumber(normalized);
//    if (opt.isEmpty()) opt = tailRepo.findByTailNumber(alt);
//
//    TailNumber t = opt.orElseThrow(() -> new EntityNotFoundException("Tail not found: " + code));
//
//    Airline airline = t.getAirline();
//    Aircraft ac = t.getAircraftType();
//
//    return new TailLookupResponse(
//      airline != null ? airline.getName() : null,
//      t.getCountry(),
//      ac != null ? ac.getModel() : null,
//      ac != null ? ac.getBrand() : null,
//      ac != null ? ac.getPaxNumber() : null,
//      ac != null ? ac.getRangeKm() : null,
//      ac != null ? ac.getSeatLayoutJson() : null
//    );
//  }
//
//  private AirportLookupResponse lookupAirport(String code) {
//    Airport ap = airportRepo.findByIataCode(code.toUpperCase())
//            .orElseThrow(() -> new EntityNotFoundException("Airport not found: " + code));
//
//    List<String> runways = runwayRepo.findByAirport(ap).stream()
//            .map(Runway::getIdentifier).toList();
//
//    List<String> airlines = flightRepo.findAirlinesServing(ap).stream()
//            .filter(Objects::nonNull)
//            .map(Airline::getName)
//            .sorted(String.CASE_INSENSITIVE_ORDER)
//            .toList();
//
////    List<String> destinations = flightRepo.findConnectedAirports(ap).stream()
////        .filter(Objects::nonNull)
////        .map(Airport::getIataCode)
////        .sorted(String.CASE_INSENSITIVE_ORDER)
////        .toList();
//
//    List<String> destinations = flightRepo.findConnectedIataCodes(ap).stream()
//            .filter(Objects::nonNull)
//            .sorted(String.CASE_INSENSITIVE_ORDER)
//            .toList();
//    ZoneId zone = ZoneId.of(ap.getTimeZoneId());
//    ZonedDateTime now = ZonedDateTime.now(zone);
//
//    List<NearbyAirportDTO> nearby = (ap.getLatitude() == null || ap.getLongitude() == null)
//            ? List.of()
//            : svc.nearestAirports(ap, 5).stream()
//            .map(n -> new NearbyAirportDTO(
//                    n.getIataCode(),
//                    n.getCity(),
//                    n.getLatitude(),
//                    n.getLongitude(),
//                    n.getDistanceKm()
//            ))
//            .toList();
//
//
//// Format: "Saturday, 25 Oct 2025, 03:40 AM IST (Asia/Kolkata)"
//    DateTimeFormatter fmt = DateTimeFormatter.ofPattern("EEEE, dd MMM yyyy, hh:mm a z '('VV')'", Locale.ENGLISH);
//    String nowLocal = now.format(fmt);
//    return new AirportLookupResponse(
//            ap.getIataCode(),
//            ap.getCity(),
//            runways,
//            airlines,
//            destinations,
//            nowLocal,
//            nearby
//    );
//  }
//
//  private static String normalizeFlight(String s) {
//    String u = s.toUpperCase(Locale.ROOT);
//    return u.replace("-", "");
//  }
//
//  private static String normalizedWithHyphen(String normalizedNoHyphen) {
//    // Insert a hyphen after first 2 chars to try alternate storage
//    if (normalizedNoHyphen.length() >= 3) {
//      return normalizedNoHyphen.substring(0, 2) + "-" + normalizedNoHyphen.substring(2);
//    }
//    return normalizedNoHyphen;
//  }
//
//  private static String normalizeTail(String s) {
//    String u = s.toUpperCase(Locale.ROOT);
//    // Prefer "AA-ABC" style as primary
//    if (!u.contains("-") && u.length() >= 3) {
//      return u.substring(0, 2) + "-" + u.substring(2);
//    }
//    return u;
//  }
//
//  // SmartLookupControllerOld.java
//  @GetMapping(value = "/{code}", produces = { MediaType.TEXT_HTML_VALUE, MediaType.APPLICATION_JSON_VALUE })
//  public ResponseEntity<?> airport(@PathVariable String code,
//                                   @RequestHeader(value = "Accept", required = false) String accept,
//                                   @RequestParam(value = "view", required = false) String view) {
//
//    String iata = code.trim().toUpperCase();
//
//    Airport ap = airportRepo.findByIataCodeIgnoreCase(iata)
//            .orElseThrow(() -> new EntityNotFoundException("Airport not found: " + iata));
//
//    var runways      = runwayRepo.findByAirport(ap);
//    var airlineNames = flightRepo.findAirlinesServingAirport(iata);    // List<String>
//    var destinations = flightRepo.findConnectedAirportIatas(iata);     // List<String>
//
//    // Local time (readable); fallback if tz missing
//    String tz = ap.getTimeZoneId();
//    String localTime = (tz == null || tz.isBlank())
//            ? "Timezone not set"
//            : ZonedDateTime.now(ZoneId.of(tz))
//            .format(DateTimeFormatter.ofPattern("EEE, dd MMM uuuu HH:mm z"));
//
//    // Nearby airports (project → DTO)
//    var nearbyProj = (ap.getLatitude() == null || ap.getLongitude() == null)
//            ? List.<AirportDistanceProjection>of()
//            : svc.nearestAirports(ap, 5);
//
//    List<NearbyAirportDTO> nearbyDtos = nearbyProj.stream()
//            .map(n -> new NearbyAirportDTO(
//                    n.getIataCode(),
//                    n.getCity(),
//                    n.getLatitude(),
//                    n.getLongitude(),
//                    n.getDistanceKm()
//            ))
//            .toList();
//
//    boolean wantsHtml = "html".equalsIgnoreCase(view) ||
//            (accept != null && accept.toLowerCase().contains("text/html"));
//
//    if (!wantsHtml) {
//      var json = new AirportLookupResponse(
//              ap.getIataCode(),
//              ap.getCity(),
//              runways.stream().map(Runway::getIdentifier).toList(),
//              airlineNames,
//              destinations,
//              localTime,
//              nearbyDtos
//      );
//      return ResponseEntity.ok(json);
//    }
//
//    // HTML view
//    String html = buildAirportHtml(ap, runways, airlineNames, destinations, localTime, nearbyDtos);
//    return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(html);
//  }
//
//  // SmartLookupControllerOld.java (private method)
//  private String buildAirportHtml(Airport ap,
//                                  List<Runway> runways,
//                                  List<String> airlineNames,
//                                  List<String> destinations,
//                                  String localTime,
//                                  List<NearbyAirportDTO> nearby) {
//
//    String tz = ap.getTimeZoneId() == null ? "" : ap.getTimeZoneId();
//    String coord = (ap.getLatitude() == null || ap.getLongitude() == null)
//            ? "N/A"
//            : String.format("%.4f, %.4f", ap.getLatitude(), ap.getLongitude());
//
//    String runwayStr = runways.isEmpty()
//            ? "—"
//            : runways.stream().map(Runway::getIdentifier).sorted()
//            .collect(java.util.stream.Collectors.joining(" / "));
//
//    String airlinesStr = airlineNames.isEmpty()
//            ? "—"
//            : airlineNames.stream().sorted()
//            .collect(java.util.stream.Collectors.joining(", "));
//
//    String destStr = destinations.isEmpty()
//            ? "—"
//            : destinations.stream().sorted()
//            .collect(java.util.stream.Collectors.joining(", "));
//
//    String nearbyRows = nearby.isEmpty()
//            ? "<tr><td colspan='4' style='opacity:.7'>No nearby airports found</td></tr>"
//            : nearby.stream().map(n -> String.format("""
//          <tr>
//            <td><a href="/%1$s">%1$s</a></td>
//            <td>%2$s</td>
//            <td>%3$.4f, %4$.4f</td>
//            <td style="text-align:right">%5$.1f km</td>
//          </tr>
//        """, safe(n.iata()), safe(n.city()), n.latitude(), n.longitude(), n.distanceKm()))
//            .collect(java.util.stream.Collectors.joining());
//
//    return """
//<!doctype html>
//<html>
//<head>
//  <meta charset="utf-8"/>
//  <meta name="viewport" content="width=device-width,initial-scale=1"/>
//  <title>%1$s • %2$s</title>
//  <style>
//    *{box-sizing:border-box}
//    body{font-family:ui-sans-serif,system-ui,Segoe UI,Inter,Arial;margin:0;background:#0b1020;color:#e7ecff}
//    .wrap{max-width:980px;margin:0 auto;padding:24px}
//    h1{margin:0 0 16px;font-size:24px}
//    table{width:100%;border-collapse:collapse;background:#121938;border:1px solid #223;border-radius:12px;overflow:hidden}
//    th,td{padding:10px 12px;border-top:1px solid #223}
//    tr:first-child td, tr:first-child th{border-top:0}
//    th{width:220px;text-align:left;color:#aab3ff;background:#121938}
//    a{color:#7fb0ff;text-decoration:none}
//    a:hover{text-decoration:underline}
//    .grid{display:grid;gap:16px;grid-template-columns:1fr}
//    .card{background:#121938;border:1px solid #223;border-radius:12px;padding:16px}
//    .sub{opacity:.85}
//    .right{text-align:right}
//  </style>
//</head>
//<body>
//  <div class="wrap">
//    <h1>About %1$s (%2$s)</h1>
//
//    <div class="grid">
//      <div class="card">
//        <table>
//          <tr><th>IATA</th><td>%1$s</td></tr>
//          <tr><th>City</th><td>%2$s</td></tr>
//          <tr><th>Runways</th><td>%3$s</td></tr>
//          <tr><th>Timezone</th><td>%4$s</td></tr>
//          <tr><th>Local time</th><td>%5$s</td></tr>
//          <tr><th>Coordinates</th><td>%6$s</td></tr>
//          <tr><th>Airlines</th><td class="sub">%7$s</td></tr>
//          <tr><th>Destinations</th><td class="sub">%8$s</td></tr>
//        </table>
//      </div>
//
//      <div class="card">
//        <h2 style="margin:0 0 8px;font-size:18px">Nearby Airports</h2>
//        <table>
//          <tr>
//            <th style="width:90px">IATA</th>
//            <th>City</th>
//            <th>Coordinates</th>
//            <th class="right" style="width:120px">Distance</th>
//          </tr>
//          %9$s
//        </table>
//      </div>
//    </div>
//  </div>
//</body>
//</html>
//""".formatted(
//            safe(ap.getIataCode()),
//            safe(ap.getCity()),
//            runwayStr,
//            safe((tz == null || tz.isBlank()) ? "—" : tz),
//            safe(localTime),
//            safe(coord),
//            airlinesStr,
//            destStr,
//            nearbyRows
//    );
//  }
//
//  private static String safe(String s){
//    return s == null ? "" : s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;");
//  }
//
//  private static String nullToDash(String s){ return (s == null || s.isBlank()) ? "—" : s; }
//
//
//}
