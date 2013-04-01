-include base.pro

-injars ../libs-full/jackson-core-asl-1.8.0.jar
-outjars ../libs/shrunk-jackson-core-asl-1.8.0.jar

-keep public class org.codehaus.jackson.JsonFactory
-keep public class org.codehaus.jackson.JsonParser
-keep public class org.codehaus.jackson.JsonToken

-keepclassmembers public class org.codehaus.jackson.JsonFactory {
  public org.codehaus.jackson.JsonParser createJsonParser(java.io.InputStream);
}

-keepclassmembers public class org.codehaus.jackson.JsonParser {
  public void close();
  public java.lang.String getText();
  public java.lang.String getCurrentName();
  public double getValueAsDouble();
  public int getValueAsInt();
  public boolean getValueAsBoolean();
  public org.codehaus.jackson.JsonParser skipChildren();
  public org.codehaus.jackson.JsonToken getCurrentToken();
  public org.codehaus.jackson.JsonToken getNextToken();
  public org.codehaus.jackson.JsonToken nextToken();
}

-keepclassmembers public class org.codehaus.jackson.JsonToken {
  *;
}

