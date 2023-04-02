// via: https://www.gwtcenter.com/handling-version-number-uniformly-by-gradle
package controlP5;

public class Version {
  public static String version = "2.3.0-SNAPSHOT";
  public static String buildDate = "2023-04-02 18:33:12";

  /**
   * バージョン情報を返す
   *
   * @return version
   */
  public static String getVersion() {
    return version;
  }

  /**
   * コンパイルした日時を返す
   *
   * @return buildDate
   */
  public static String getBuildDate() {
    return buildDate;
  }
}
