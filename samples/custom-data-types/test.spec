import org.joda.time.DateTime
def someDate = new DateTime()

service {
  name "test"
  version "$someDate-$someName"

  tests {
    scenario "test with joda" spec {
      new DateTime()
    }
  }
}
