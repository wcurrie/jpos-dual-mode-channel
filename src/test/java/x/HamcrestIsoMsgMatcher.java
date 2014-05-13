package x;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.jpos.iso.ISOMsg;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class HamcrestIsoMsgMatcher extends TypeSafeMatcher<ISOMsg> {

    private final ISOMsg expected;

    public HamcrestIsoMsgMatcher(ISOMsg expected) {
        this.expected = expected;
    }

    @Override
    protected boolean matchesSafely(ISOMsg item) {
        return dump(expected).equals(dump(item));
    }

    @Override
    public void describeTo(Description description) {
        description.appendText(dump(expected));
    }

    @Override
    protected void describeMismatchSafely(ISOMsg item, Description mismatchDescription) {
        mismatchDescription.appendText(dump(item));
    }

    public static Matcher<ISOMsg> isIsoMsg(ISOMsg expected) {
        return new HamcrestIsoMsgMatcher(expected);
    }

    public static String dump(ISOMsg m) {
        if (m == null) {
            return "<null>";
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        cleanCopy(m).dump(new PrintStream(out), "");
        return out.toString();
    }

    private static ISOMsg cleanCopy(ISOMsg m) {
        ISOMsg copy = (ISOMsg) m.clone();
        copy.setDirection(0);
        copy.setPackager(null);
        return copy;
    }
}
