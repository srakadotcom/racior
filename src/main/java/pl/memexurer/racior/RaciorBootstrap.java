package pl.memexurer.racior;

public class RaciorBootstrap {

  private RaciorBootstrap() {
  }

  public static void main(String[] args) throws Throwable {
    if(args.length != 1)
    {
      System.out.println("Dopisz jeszcze token bota do argumentow i bedzie zajebiscie");
      return;
    }
    new Racior().startJda(args[0]);
  }

}
