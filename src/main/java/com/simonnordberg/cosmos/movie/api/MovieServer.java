package com.simonnordberg.cosmos.movie.api;

import com.google.common.base.Strings;
import com.simonnordberg.cosmos.api.Movie;
import com.simonnordberg.cosmos.api.MovieQuery;
import com.simonnordberg.cosmos.api.MovieServiceGrpc.MovieServiceImplBase;
import com.simonnordberg.cosmos.api.MoviesQuery;
import io.grpc.Metadata;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.ServerCall;
import io.grpc.ServerCall.Listener;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.Status;
import io.grpc.protobuf.services.HealthStatusManager;
import io.grpc.protobuf.services.ProtoReflectionService;
import io.grpc.stub.StreamObserver;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class MovieServer {

  private static final Logger logger = Logger.getLogger(MovieServer.class.getName());

  private Server server;

  private void start() throws IOException {
    int port = 50051;
    HealthStatusManager healthStatusManager = new HealthStatusManager();
    server = ServerBuilder.forPort(port).addService(new MovieServiceImpl())
        .addService(healthStatusManager.getHealthService())
        .addService(ProtoReflectionService.newInstance()).intercept(new ServerInterceptor() {
          @Override
          public <ReqT, RespT> Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> serverCall,
              Metadata metadata, ServerCallHandler<ReqT, RespT> next) {
            logger.info(String.format("""
                    Received request ->
                      MethodDescriptor (%s),
                      Metadata (%s)
                      Attributes (%s)""",
                serverCall.getMethodDescriptor(), metadata, serverCall.getAttributes()));
            logger.info(serverCall.getAttributes().toString());
            return next.startCall(serverCall, metadata);
          }
        }).build().start();
    logger.info("Server started, listening on " + port);
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      System.err.println("*** Shutting down gRPC server since JVM is shutting down");
      try {
        MovieServer.this.stop();
      } catch (InterruptedException e) {
        e.printStackTrace(System.err);
      }
      System.err.println("*** Server shut down");
    }));
  }

  private void stop() throws InterruptedException {
    if (server != null) {
      server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
    }
  }

  private void blockUntilShutdown() throws InterruptedException {
    if (server != null) {
      server.awaitTermination();
    }
  }

  public static void main(String[] args) throws IOException, InterruptedException {
    final MovieServer server = new MovieServer();
    server.start();
    server.blockUntilShutdown();
  }

  static class MovieServiceImpl extends MovieServiceImplBase {

    @Override
    public void getMovies(MoviesQuery request, StreamObserver<Movie> responseObserver) {
      System.out.println("Received request: " + request);
      String query = request.getQuery();

      if (Strings.isNullOrEmpty(query)) {
        Status status = Status.FAILED_PRECONDITION.withDescription("Query is required");
        responseObserver.onError(status.asRuntimeException());
        return;
      }

      MovieServer.getMovies().stream()
          .filter(movie -> movie.getName().matches(String.format("(?i).*%s.*", query)))
          .forEach(responseObserver::onNext);
      responseObserver.onCompleted();
    }

    @Override
    public void getMovie(MovieQuery request, StreamObserver<Movie> responseObserver) {
      String id = request.getId();
      if (Strings.isNullOrEmpty(id)) {
        Status status = Status.FAILED_PRECONDITION.withDescription("Id is required");
        responseObserver.onError(status.asRuntimeException());
        return;
      }

      MovieServer.getMovies().stream().filter(movie -> movie.getId().equals(id)).findFirst()
          .ifPresentOrElse(responseObserver::onNext,
              () -> responseObserver.onError(Status.NOT_FOUND.asRuntimeException()));
      responseObserver.onCompleted();
    }
  }

  private static List<Movie> getMovies() {
    return Arrays.asList(
        Movie.newBuilder().setId("01d3ca56-94c0-4452-bd5b-e55ed7302f62").setName("Vertigo (1958)")
            .build(),

        Movie.newBuilder().setId("beaf9d3e-c003-434a-9e61-be3f5bd6b4db")
            .setName("Le fabuleux destin d'Am�lie Poulain (2001)").build(),

        Movie.newBuilder().setId("6cbe34cd-90f0-4cec-8c43-ba9a7d3575e2").setName("Alien (1979)")
            .build(),

        Movie.newBuilder().setId("2b773942-91a1-4b10-bd0b-17db19a06c3a").setName("WALL�E (2008)")
            .build(),

        Movie.newBuilder().setId("0164cde8-4fcc-403d-b9b3-d9df736608c7")
            .setName("Lawrence of Arabia (1962)").build(),

        Movie.newBuilder().setId("0d31e5f7-85d6-49cf-b5ce-45aabab5e05a")
            .setName("The Shining (1980)").build(),

        Movie.newBuilder().setId("cc94aaed-84f8-4976-932a-cef4312af222")
            .setName("Sen to Chihiro no kamikakushi (2001)").build(),

        Movie.newBuilder().setId("563ec219-8a84-4dc9-a65f-e54f8a45fe79")
            .setName("Paths of Glory (1957)").build(),

        Movie.newBuilder().setId("e0dc1008-4602-45fd-b436-6c32437d5830")
            .setName("A Clockwork Orange (1971)").build(),

        Movie.newBuilder().setId("372179d4-7722-4e69-91bb-b21dd8b885b2")
            .setName("Double Indemnity (1944)").build(),

        Movie.newBuilder().setId("6b3ce168-283f-4162-918a-18425d06278b")
            .setName("To Kill a Mockingbird (1962)").build(),

        Movie.newBuilder().setId("e7b76001-fb3f-4003-b538-bc8ef63066e5")
            .setName("The Pianist (2002)").build(),

        Movie.newBuilder().setId("7cb0d938-ef1c-46f3-9a5a-081927576c2e")
            .setName("Das Leben der Anderen (2006)").build(),

        Movie.newBuilder().setId("99edf92a-ccfd-44ad-8d51-723ea505c265")
            .setName("The Departed (2006)").build(),

        Movie.newBuilder().setId("0b6a657d-161f-4755-b8c3-405da163f270").setName("M (1931)")
            .build(),

        Movie.newBuilder().setId("cdbb3e91-439a-4fa6-ad37-068f0fe10d11")
            .setName("City Lights (1931)").build(),

        Movie.newBuilder().setId("6f6d0fcb-93c6-4b02-a11b-a0b36f1bddeb").setName("Aliens (1986)")
            .build(),

        Movie.newBuilder().setId("ffd5bad9-c9e1-49d3-be6f-be50c183bcf2")
            .setName("Eternal Sunshine of the Spotless Mind (2004)").build(),

        Movie.newBuilder().setId("0fa55540-8b31-4935-b5bf-34a687ac28f0")
            .setName("Requiem for a Dream (2000)").build(),

        Movie.newBuilder().setId("409c3e51-df4a-414c-aa15-2ec496ec66ef").setName("Das Boot (1981)")
            .build(),

        Movie.newBuilder().setId("90158b5e-80af-417f-a033-cf919b0c0edb")
            .setName("The Third Man (1949)").build(),

        Movie.newBuilder().setId("1f2ce260-055b-4949-8642-b6282f843d2a")
            .setName("L.A. Confidential (1997)").build(),

        Movie.newBuilder().setId("52a6849c-1e55-40cd-acb0-8129861c953a")
            .setName("Reservoir Dogs (1992)").build(),

        Movie.newBuilder().setId("7021368e-bdad-4c3e-902a-1abdb3cea3e3").setName("Chinatown (1974)")
            .build(),

        Movie.newBuilder().setId("5dc87be5-5ae8-4ccf-b953-2a2fd5bc5ae0")
            .setName("The Treasure of the Sierra Madre (1948)").build(),

        Movie.newBuilder().setId("36089afc-732e-404a-8eb8-4ba5506ddd95")
            .setName("Modern Times (1936)").build(),

        Movie.newBuilder().setId("80827bfb-812c-4968-b60f-c1f2f98e398a")
            .setName("Monty Python and the Holy Grail (1975)").build(),

        Movie.newBuilder().setId("077631ca-e292-4ceb-8e0c-c390cb08b13b")
            .setName("La vita � bella (1997)").build(),

        Movie.newBuilder().setId("3366cb18-b185-4d79-b1ac-afc49633c5a2")
            .setName("Back to the Future (1985)").build(),

        Movie.newBuilder().setId("7771ae66-ea52-462d-88ae-236dbafcbc6d")
            .setName("The Prestige (2006)").build(),

        Movie.newBuilder().setId("0db18277-8afb-4842-8e92-4b4089697720")
            .setName("El laberinto del fauno (2006)").build(),

        Movie.newBuilder().setId("b2b5dc27-c874-4e90-b5b8-b79e1909d492")
            .setName("Raging Bull (1980)").build(),

        Movie.newBuilder().setId("c2a81492-a009-4d18-8b1c-2323c97d3e28")
            .setName("Nuovo Cinema Paradiso (1988)").build(),

        Movie.newBuilder().setId("978b272a-1ab8-4b70-ae09-d9e80e83dee8")
            .setName("Singin' in the Rain (1952)").build(),

        Movie.newBuilder().setId("6c7e85c4-e409-4300-99a1-14c4981c03a8")
            .setName("Some Like It Hot (1959)").build(),

        Movie.newBuilder().setId("f15d7f86-2f5c-483d-b5e2-85572b7af5a0")
            .setName("The Bridge on the River Kwai (1957)").build(),

        Movie.newBuilder().setId("12e28a9c-555a-4d83-b0ae-950710788be5").setName("Rash�mon (1950)")
            .build(),

        Movie.newBuilder().setId("05871c32-fcab-4894-862d-0088c307b470")
            .setName("All About Eve (1950)").build(),

        Movie.newBuilder().setId("7171e713-d149-48cb-979f-b16371eead50").setName("Amadeus (1984)")
            .build(),

        Movie.newBuilder().setId("c16a125c-e096-4893-89b3-46dbfe832c8f")
            .setName("Once Upon a Time in America (1984)").build(),

        Movie.newBuilder().setId("2732c940-5b55-4581-9b8b-ce80383e953d")
            .setName("The Green Mile (1999)").build(),

        Movie.newBuilder().setId("8f33259e-fe16-448a-b81e-5c14e076d39f")
            .setName("Full Metal Jacket (1987)").build(),

        Movie.newBuilder().setId("d88d8864-1cb6-4ed3-a7fe-60e2bc8adb04")
            .setName("Inglourious Basterds (2009)").build(),

        Movie.newBuilder().setId("b64c0c85-578a-417b-8db5-c80b3a2e582f")
            .setName("2001: A Space Odyssey (1968)").build(),

        Movie.newBuilder().setId("bdbd1dc7-78ef-4d91-9534-0c748efbbfc3")
            .setName("The Great Dictator (1940)").build(),

        Movie.newBuilder().setId("04e0baff-fa10-4203-834e-61b63a3050d7")
            .setName("Braveheart (1995)").build(),

        Movie.newBuilder().setId("3021a9bd-8e48-4fce-b0a6-e123537f722a")
            .setName("Ladri di biciclette (1948)").build(),

        Movie.newBuilder().setId("bbd29b02-a7ce-4ef1-8dea-78cdaa46a2f1")
            .setName("The Apartment (1960)").build(),

        Movie.newBuilder().setId("0aa614de-7aec-4a05-b044-ad48d17ac798").setName("Up (2009)")
            .build(),

        Movie.newBuilder().setId("c42940ef-6366-46d8-a045-721b79beb685")
            .setName("Der Untergang (2004)").build(),

        Movie.newBuilder().setId("d955a288-ffd1-4cc2-a247-fdfb02b8294c")
            .setName("Gran Torino (2008)").build(),

        Movie.newBuilder().setId("497c0925-3c85-442f-8918-62457a7b9dc7")
            .setName("Metropolis (1927)").build(),

        Movie.newBuilder().setId("912d4f6e-f3ee-411a-9ef0-4e0add3ae414").setName("The Sting (1973)")
            .build(),

        Movie.newBuilder().setId("ac3a6254-0232-4788-b0c5-414fbe317f86").setName("Gladiator (2000)")
            .build(),

        Movie.newBuilder().setId("860c2e41-0467-4241-9244-dbfe8864b53c")
            .setName("The Maltese Falcon (1941)").build(),

        Movie.newBuilder().setId("7146b18b-fccc-41e0-866c-9e5fb21e7490")
            .setName("Unforgiven (1992)").build(),

        Movie.newBuilder().setId("2bf5dd47-9d8f-41d7-8190-967c102f2e94").setName("Sin City (2005)")
            .build(),

        Movie.newBuilder().setId("fa824da3-90de-4b3d-88f0-dd4a6004dd8d")
            .setName("The Elephant Man (1980)").build(),

        Movie.newBuilder().setId("d8512e4d-8467-4f98-8822-44c310c4c487")
            .setName("Mr. Smith Goes to Washington (1939)").build(),

        Movie.newBuilder().setId("4393c831-e9f1-4cbb-98b6-124b7c1ecf84").setName("Oldeuboi (2003)")
            .build(),

        Movie.newBuilder().setId("5c5805b3-6954-44b3-a7e1-676ddbc9a99b")
            .setName("On the Waterfront (1954)").build(),

        Movie.newBuilder().setId("db76df9f-9d31-4c2f-9659-b99d934d6d8c")
            .setName("Indiana Jones and the Last Crusade (1989)").build(),

        Movie.newBuilder().setId("460881ae-5c8f-4205-9aea-eea54a14f171")
            .setName("Star Wars: Episode VI - Return of the Jedi (1983)").build(),

        Movie.newBuilder().setId("e44a617e-0a26-4f7d-a2ba-6693165efa80").setName("Rebecca (1940)")
            .build(),

        Movie.newBuilder().setId("0cf7bb72-438c-4ac8-8bc9-df91261421b0")
            .setName("The Great Escape (1963)").build(),

        Movie.newBuilder().setId("c6c102f6-265d-4363-93da-c1b55d51a240").setName("Die Hard (1988)")
            .build(),

        Movie.newBuilder().setId("0d140f79-06de-49b0-8ff7-e0d68b7d87b5")
            .setName("Batman Begins (2005)").build(),

        Movie.newBuilder().setId("79b9060e-0807-405b-925e-8f542d8fa367")
            .setName("Mononoke-hime (1997)").build(),

        Movie.newBuilder().setId("9305a1bd-fecb-45a1-ae49-7584a2fb7df8").setName("Jaws (1975)")
            .build(),

        Movie.newBuilder().setId("f338da42-2d8c-4020-ad0d-15ebb1ed3042")
            .setName("Hotel Rwanda (2004)").build(),

        Movie.newBuilder().setId("4f14c050-98aa-426f-9cfd-0d383a027166")
            .setName("Slumdog Millionaire (2008)").build(),

        Movie.newBuilder().setId("24a2e315-ae87-44cf-8179-297a766853c5")
            .setName("Det sjunde inseglet (1957)").build(),

        Movie.newBuilder().setId("425abba8-cee0-483c-9fa4-a15d2bcaf4a1")
            .setName("Blade Runner (1982)").build(),

        Movie.newBuilder().setId("31f5a4f7-8087-4b87-b396-2d7c0a8a1fdb").setName("Fargo (1996)")
            .build(),

        Movie.newBuilder().setId("2559deca-14ec-4b70-9aeb-17ea427a6199")
            .setName("No Country for Old Men (2007)").build(),

        Movie.newBuilder().setId("3ca7ae0b-6270-4c0d-9793-02bb4dc4dd0d").setName("Heat (1995)")
            .build(),

        Movie.newBuilder().setId("fe43562e-77d6-4ef0-8cd6-7899aa74e1b6")
            .setName("The General (1926)").build(),

        Movie.newBuilder().setId("65d60294-4110-473d-9426-c1312820cc1d")
            .setName("The Wizard of Oz (1939)").build(),

        Movie.newBuilder().setId("cd43aa44-9b18-473d-82d6-0ab704b924d9")
            .setName("Touch of Evil (1958)").build(),

        Movie.newBuilder().setId("79aaedd4-8f11-4567-9bf3-06f117137f92")
            .setName("Per qualche dollaro in pi� (1965)").build(),

        Movie.newBuilder().setId("fe534bf7-62c9-4186-8229-9a995410a721").setName("Ran (1985)")
            .build(),

        Movie.newBuilder().setId("6c4140cc-e869-45b6-9d57-a6f845b750bb").setName("Y�jinb� (1961)")
            .build(),

        Movie.newBuilder().setId("5ecb2ffb-4e38-45af-8560-6635fb83ec53")
            .setName("District 9 (2009)").build(),

        Movie.newBuilder().setId("5ded9ef4-c35a-4554-adc9-5e14d5c9ff9c")
            .setName("The Sixth Sense (1999)").build(),

        Movie.newBuilder().setId("01610b1b-10d7-4b05-98f6-2c5d6d830306").setName("Snatch. (2000)")
            .build(),

        Movie.newBuilder().setId("47af1610-64ea-49e5-90d6-07a908f6bfad")
            .setName("Donnie Darko (2001)").build(),

        Movie.newBuilder().setId("f97e78aa-48ae-4e12-80fc-f6b46a229f25")
            .setName("Annie Hall (1977)").build(),

        Movie.newBuilder().setId("f03e0c42-342e-459a-aba9-7c7b8d83fa6c")
            .setName("Witness for the Prosecution (1957)").build(),

        Movie.newBuilder().setId("50f40b0c-e34d-479c-bbc0-f74b674dcfbf")
            .setName("Smultronst�llet (1957)").build(),

        Movie.newBuilder().setId("17bb2505-8c00-4fd2-b975-5f48e9a1612e")
            .setName("The Deer Hunter (1978)").build(),

        Movie.newBuilder().setId("0a92f0da-4869-4010-939b-a9fc2121be7c").setName("Avatar (2009)")
            .build(),

        Movie.newBuilder().setId("7a8c1d53-ff34-4030-94a2-d0c0c3538b72")
            .setName("The Social Network (2010)").build(),

        Movie.newBuilder().setId("71cbd5b2-be39-4a5a-963d-09e5e520bedd")
            .setName("Cool Hand Luke (1967)").build(),

        Movie.newBuilder().setId("d7c4457d-9c00-4f55-8573-aef921a1622e")
            .setName("Strangers on a Train (1951)").build(),

        Movie.newBuilder().setId("9ee24a75-b712-4fc2-9bcc-403e82349307").setName("High Noon (1952)")
            .build(),

        Movie.newBuilder().setId("8019e345-7390-42a5-9978-22ec58c557c2")
            .setName("The Big Lebowski (1998)").build(),

        Movie.newBuilder().setId("daff8b39-275a-4a35-aa81-ed83c3e776b3")
            .setName("Hotaru no haka (1988)").build(),

        Movie.newBuilder().setId("6d701c3e-1905-41c0-8cdd-8fc4301f8829")
            .setName("Kill Bill: Vol. 1 (2003)").build(),

        Movie.newBuilder().setId("2773daa5-c4fb-426a-8dbb-fe8cc615b27f")
            .setName("It Happened One Night (1934)").build(),

        Movie.newBuilder().setId("035601ca-3015-43c6-abc8-78d1008769ae").setName("Platoon (1986)")
            .build(),

        Movie.newBuilder().setId("165e78bb-4115-452a-909b-d605b7c47514")
            .setName("The Lion King (1994)").build(),

        Movie.newBuilder().setId("75038a33-98c5-4c79-83a2-ae9fc637404c")
            .setName("Into the Wild (2007)").build(),

        Movie.newBuilder().setId("60f2671c-6e55-43fc-b60c-fd08cf964d2c")
            .setName("There Will Be Blood (2007)").build(),

        Movie.newBuilder().setId("439a5114-509e-4b4c-b345-c8d09d56d7e3").setName("Notorious (1946)")
            .build(),

        Movie.newBuilder().setId("f766e82c-d998-46d2-b62b-c4e6018caa30")
            .setName("Million Dollar Baby (2004)").build(),

        Movie.newBuilder().setId("268f5ab3-4839-4134-a6df-3cb46b0641e9").setName("Toy Story (1995)")
            .build(),

        Movie.newBuilder().setId("31758b75-c631-4130-9a59-c37ba5adee3a")
            .setName("Butch Cassidy and the Sundance Kid (1969)").build(),

        Movie.newBuilder().setId("ada31783-f2f6-4d28-a225-a046627cf9e2")
            .setName("Gone with the Wind (1939)").build(),

        Movie.newBuilder().setId("7c8fa6bb-8a49-41f8-bc2c-b9cf83fe73b5")
            .setName("Sunrise: A Song of Two Humans (1927)").build(),

        Movie.newBuilder().setId("1caba532-8cea-45c1-93c5-fb60301fa673")
            .setName("The Wrestler (2008)").build(),

        Movie.newBuilder().setId("fde94812-e358-4a72-9260-c728643e84c2")
            .setName("The Manchurian Candidate (1962)").build(),

        Movie.newBuilder().setId("8fc09674-929c-4007-8726-2e8fe96e485e")
            .setName("Trainspotting (1996)").build(),

        Movie.newBuilder().setId("01cee4ab-392a-4e33-bc08-46294268a197").setName("Ben-Hur (1959)")
            .build(),

        Movie.newBuilder().setId("c3960238-996f-4d17-80f1-d25e2561ec10").setName("Scarface (1983)")
            .build(),

        Movie.newBuilder().setId("974d8bbc-56a7-4a87-80f0-5024617ef389")
            .setName("The Grapes of Wrath (1940)").build(),

        Movie.newBuilder().setId("d941d720-ad92-4232-a77f-e1a198db1031")
            .setName("The Graduate (1967)").build(),

        Movie.newBuilder().setId("bd7e08ee-d5eb-4a42-9260-46b6985f899a")
            .setName("The Big Sleep (1946)").build(),

        Movie.newBuilder().setId("5bb0d53c-ff39-4c41-b327-7f3738d73276")
            .setName("Groundhog Day (1993)").build(),

        Movie.newBuilder().setId("35617a02-9bae-47a2-b834-12cbbf89fc65")
            .setName("Life of Brian (1979)").build(),

        Movie.newBuilder().setId("80d1fea5-7a4b-4ab1-8f1f-5ee360166a8b")
            .setName("The Gold Rush (1925)").build(),

        Movie.newBuilder().setId("cbef1fb8-ef54-4a92-beb1-86d6daee5c6e")
            .setName("The Bourne Ultimatum (2007)").build(),

        Movie.newBuilder().setId("7b342f92-a67d-4c6b-a478-6529558bd3a5")
            .setName("Amores perros (2000)").build(),

        Movie.newBuilder().setId("9da4b6f6-cdf2-435c-a268-0bc7f9b463ac")
            .setName("Finding Nemo (2003)").build(),

        Movie.newBuilder().setId("395b23fc-f4d8-485c-95c8-833e8a843126")
            .setName("The Terminator (1984)").build(),

        Movie.newBuilder().setId("9d619ae8-9e57-460e-9d3f-309ce5b3b316")
            .setName("Stand by Me (1986)").build(),

        Movie.newBuilder().setId("5ce71dd7-e250-40f4-bebc-7d483f802004")
            .setName("How to Train Your Dragon (2010)").build(),

        Movie.newBuilder().setId("ec20e34e-182c-48b1-a157-2e0b98776c89")
            .setName("The Best Years of Our Lives (1946)").build(),

        Movie.newBuilder().setId("c1ce0d09-fb42-4a9c-ac61-59772753385e")
            .setName("Lock, Stock and Two Smoking Barrels (1998)").build(),

        Movie.newBuilder().setId("c7022c4a-6513-45d5-8f7c-5f07f542793d").setName("The Thing (1982)")
            .build(),

        Movie.newBuilder().setId("d16eb848-1a3a-42e1-885c-718cc2be770a").setName("The Kid (1921)")
            .build(),

        Movie.newBuilder().setId("9fb26032-4fa8-44ab-8e76-d654f4d50553")
            .setName("V for Vendetta (2006)").build(),

        Movie.newBuilder().setId("59251aeb-2e24-42ef-9798-0342a84a8ffe").setName("Casino (1995)")
            .build(),

        Movie.newBuilder().setId("e98d919a-cbad-4e3d-8b9f-36d33e3ecacb")
            .setName("Twelve Monkeys (1995)").build(),

        Movie.newBuilder().setId("f8328cf6-41f7-4977-8862-667e180da33e")
            .setName("Dog Day Afternoon (1975)").build(),

        Movie.newBuilder().setId("e0370c2c-5e23-4605-a310-3eb12daef90f")
            .setName("Ratatouille (2007)").build(),

        Movie.newBuilder().setId("91a4e592-7501-44b5-a6e9-54349d8c111c")
            .setName("El secreto de sus ojos (2009)").build(),

        Movie.newBuilder().setId("3c803ee8-69c6-4502-bc3a-950e884da6cf").setName("Gandhi (1982)")
            .build(),

        Movie.newBuilder().setId("a762a74a-d75c-4125-842a-19d28c80d2d1").setName("Star Trek (2009)")
            .build(),

        Movie.newBuilder().setId("cd28854f-e93a-44ea-b69c-cf9950a6d1a0").setName("Ikiru (1952)")
            .build(),

        Movie.newBuilder().setId("33a89b5b-b022-4646-8867-f3ce12263086")
            .setName("Le salaire de la peur (1953)").build(),

        Movie.newBuilder().setId("3552ef01-00be-4de9-934d-f94372f9f463")
            .setName("Les diaboliques (1955)").build(),

        Movie.newBuilder().setId("da28fb2f-71dc-47bd-8cee-7a16cdaaaf8b").setName("8� (1963)")
            .build(),

        Movie.newBuilder().setId("64484aa7-16ea-4001-b40e-386228f0aad6")
            .setName("The Princess Bride (1987)").build(),

        Movie.newBuilder().setId("24dced84-d2e8-4bac-bdb6-512aeeab0b8a")
            .setName("The Night of the Hunter (1955)").build(),

        Movie.newBuilder().setId("d943052c-7fce-4ca4-8d89-3486a1ed0b6d")
            .setName("Judgment at Nuremberg (1961)").build(),

        Movie.newBuilder().setId("aedfb24e-cc67-4a88-b01d-831f9c9ba5e7")
            .setName("The Incredibles (2004)").build(),

        Movie.newBuilder().setId("dbac4bf8-f6ff-450a-90f2-5167b5881328")
            .setName("Tonari no Totoro (1988)").build(),

        Movie.newBuilder().setId("25161807-764c-4b9d-9c19-7339968ed888")
            .setName("The Hustler (1961)").build(),

        Movie.newBuilder().setId("61c03225-676f-4887-b030-bde27ed15e15")
            .setName("Good Will Hunting (1997)").build(),

        Movie.newBuilder().setId("465a6a5b-bf36-4595-9848-949b5ef06e4a")
            .setName("The Killing (1956)").build(),

        Movie.newBuilder().setId("69848508-654f-4450-92f2-8a94489a1c0d").setName("In Bruges (2008)")
            .build(),

        Movie.newBuilder().setId("237d78cf-7b45-470f-9b34-1c13b05d8fb4")
            .setName("The Wild Bunch (1969)").build(),

        Movie.newBuilder().setId("259b88cf-a03e-47cc-bafd-f767231692c1").setName("Network (1976)")
            .build(),

        Movie.newBuilder().setId("962e3a29-f75a-468e-ac4c-6aafaecb3085")
            .setName("Le scaphandre et le papillon (2007)").build(),

        Movie.newBuilder().setId("d94f368f-325c-4da7-9657-e88e3c0066eb")
            .setName("A Streetcar Named Desire (1951)").build(),

        Movie.newBuilder().setId("ee3d0106-532d-4517-9c60-39cb334fda26")
            .setName("Les quatre cents coups (1959)").build(),

        Movie.newBuilder().setId("f53f933f-209e-43a9-b1a3-ac9e34d64dc0").setName("La strada (1954)")
            .build(),

        Movie.newBuilder().setId("46374beb-4485-46c4-9d16-5342814d431c")
            .setName("The Exorcist (1973)").build(),

        Movie.newBuilder().setId("52508272-f893-44aa-a794-6180ac15c004")
            .setName("Children of Men (2006)").build(),

        Movie.newBuilder().setId("97cd88ef-5370-4198-b25f-b9b942f82e66").setName("Stalag 17 (1953)")
            .build(),

        Movie.newBuilder().setId("4e1d0b46-4bb1-4697-9a89-03bf14cbff0e").setName("Persona (1966)")
            .build(),

        Movie.newBuilder().setId("b96c9ea5-406d-4dce-9b2a-56739bd34455")
            .setName("Who's Afraid of Virginia Woolf? (1966)").build(),

        Movie.newBuilder().setId("63b0fcf2-de99-4cca-b4eb-4fcc37ad4fba").setName("Ed Wood (1994)")
            .build(),

        Movie.newBuilder().setId("f312ed92-a17f-44a9-a54a-1f3ccb38d13d")
            .setName("Dial M for Murder (1954)").build(),

        Movie.newBuilder().setId("e5e2dd9c-6bc3-4bd6-b2f4-7fc57c8e58ec")
            .setName("La battaglia di Algeri (1966)").build(),

        Movie.newBuilder().setId("d822d321-61f8-419b-917a-91f76c29c3a8")
            .setName("L�t den r�tte komma in (2008)").build(),

        Movie.newBuilder().setId("9374d208-f661-4fbd-a827-4655ae873c8a")
            .setName("All Quiet on the Western Front (1930)").build(),

        Movie.newBuilder().setId("b55e7d5d-735e-46e4-b18d-a697dc6b8d9b").setName("Big Fish (2003)")
            .build(),

        Movie.newBuilder().setId("02fcd5fc-c36f-47e6-b96a-75cdcaecdb63").setName("Magnolia (1999)")
            .build(),

        Movie.newBuilder().setId("90cc6283-762c-4068-993e-4bbbc033e16a").setName("Rocky (1976)")
            .build(),

        Movie.newBuilder().setId("5aa91b82-87c8-4930-925e-3225ea04ad27")
            .setName("La passion de Jeanne d'Arc (1928)").build(),

        Movie.newBuilder().setId("1bd2662f-12b0-47a2-b294-c0e640edbd2c")
            .setName("Kind Hearts and Coronets (1949)").build(),

        Movie.newBuilder().setId("02608e77-1e78-4d1d-9fea-79e227c58ce1")
            .setName("Fanny och Alexander (1982)").build(),

        Movie.newBuilder().setId("680a544d-31e0-4a94-a087-94aeb4cee258")
            .setName("Mystic River (2003)").build(),

        Movie.newBuilder().setId("8b72946a-a150-4863-9811-38f5af5f1dbc").setName("Manhattan (1979)")
            .build(),

        Movie.newBuilder().setId("2766ffa5-f40b-44e5-81c9-186a2f049883")
            .setName("Barry Lyndon (1975)").build(),

        Movie.newBuilder().setId("5e31dc83-fa79-4889-b8b4-0b1c0b07a78d")
            .setName("Kill Bill: Vol. 2 (2004)").build(),

        Movie.newBuilder().setId("cd3d8c23-418e-434f-b43f-51eaf98622d2")
            .setName("Mary and Max (2009)").build(),

        Movie.newBuilder().setId("a172abbf-b05b-4b66-81e3-3550dcde5722").setName("Patton (1970)")
            .build(),

        Movie.newBuilder().setId("16704a2a-0915-4ebb-87c7-604812104d09")
            .setName("Rosemary's Baby (1968)").build(),

        Movie.newBuilder().setId("040df0ab-feb8-4974-8484-32d234f8d2d5").setName("Duck Soup (1933)")
            .build(),

        Movie.newBuilder().setId("131d78db-6416-4f09-a1e8-149a23bdae20").setName("Festen (1998)")
            .build(),

        Movie.newBuilder().setId("e77c1adb-52a1-41b9-9084-5919eeb362b8").setName("Kick-Ass (2010)")
            .build(),

        Movie.newBuilder().setId("e1bd11a6-d5fc-4c27-8944-3504adf8c3cb")
            .setName("Fa yeung nin wa (2000)").build(),

        Movie.newBuilder().setId("aacbba15-a543-43c4-bec1-30558a8ab248")
            .setName("Letters from Iwo Jima (2006)").build(),

        Movie.newBuilder().setId("24e633c0-934d-4e95-a68b-8159abb0df98")
            .setName("Roman Holiday (1953)").build(),

        Movie.newBuilder().setId("b50ccace-8895-4fe0-8ad8-420e1c764f05")
            .setName("Pirates of the Caribbean: The Curse of the Black Pearl (2003)").build(),

        Movie.newBuilder().setId("053a25a6-e02c-4702-bd43-4da90977f666")
            .setName("Mou gaan dou (2002)").build(),

        Movie.newBuilder().setId("c91f0b4b-122d-4afa-b4f3-3c9b1164e7b8")
            .setName("The Truman Show (1998)").build(),

        Movie.newBuilder().setId("60ecff0d-03dc-44bf-b7df-ccc026fa3c3b").setName("Crash (2004/I)")
            .build(),

        Movie.newBuilder().setId("29f1048e-cfb7-43f1-9d6c-eb55250de0dd")
            .setName("Hauru no ugoku shiro (2004)").build(),

        Movie.newBuilder().setId("16a0d3e0-d9a7-4887-821d-6a0658e6d3cb")
            .setName("His Girl Friday (1940)").build(),

        Movie.newBuilder().setId("de0240cc-5630-416a-af3a-5b065ebd98e0")
            .setName("Arsenic and Old Lace (1944)").build(),

        Movie.newBuilder().setId("1828a16e-c079-4004-91de-70ad06fbdd18").setName("Harvey (1950)")
            .build(),

        Movie.newBuilder().setId("f2dc092d-cfc7-40bc-934c-cd81e9d14764")
            .setName("Le notti di Cabiria (1957)").build(),

        Movie.newBuilder().setId("12765f91-96d5-41d3-a22a-dc46de9d402a")
            .setName("Trois couleurs: Rouge (1994)").build(),

        Movie.newBuilder().setId("08552959-2a01-4913-94b4-0c4deba6fb18")
            .setName("The Philadelphia Story (1940)").build(),

        Movie.newBuilder().setId("fc87e078-02ea-442c-b075-6b38ca142f18")
            .setName("A Christmas Story (1983)").build(),

        Movie.newBuilder().setId("787aa8c3-c86b-4cf6-b6c2-f776b287586f").setName("Sleuth (1972)")
            .build(),

        Movie.newBuilder().setId("eb76907e-118b-4a15-9fc4-2a6b09dc1cd8").setName("King Kong (1933)")
            .build(),

        Movie.newBuilder().setId("26c85568-0625-4d5d-8dda-b07e2a652301")
            .setName("Bom yeoreum gaeul gyeoul geurigo bom (2003)").build(),

        Movie.newBuilder().setId("ba7f6932-12a9-4aac-b891-e32252e1f16c").setName("Rope (1948)")
            .build(),

        Movie.newBuilder().setId("1592a8ca-81be-4cf2-88e0-b7b3a193623c")
            .setName("Monsters, Inc. (2001)").build(),

        Movie.newBuilder().setId("cfc85ba1-de01-4ba7-9f6b-3a0b78524481")
            .setName("Tenk� no shiro Rapyuta (1986)").build(),

        Movie.newBuilder().setId("03a0fbb5-f857-46c0-97a8-9d113120d0c6")
            .setName("Yeopgijeogin geunyeo (2001)").build(),

        Movie.newBuilder().setId("8cb5affe-723d-4bda-b4c3-92f1a23f1e03")
            .setName("Mulholland Dr. (2001)").build(),

        Movie.newBuilder().setId("28d46c48-67ba-4f03-a973-fa5c4bfdf5f1")
            .setName("The Man Who Shot Liberty Valance (1962)").build());
  }
}
