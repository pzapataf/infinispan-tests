import org.infinispan.protostream.MessageMarshaller;

import java.io.IOException;

public class SomethingMarshaller implements MessageMarshaller<Something> {

    @Override
    public String getTypeName() {
        return "Something";
    }

    @Override
    public Class<? extends Something> getJavaClass() {
        return Something.class;
    }

    @Override
    public void writeTo(ProtoStreamWriter writer, Something something) throws IOException {
        writer.writeString("key", something.getKey());
        writer.writeString("value", something.getValue());
    }

    @Override
    public Something readFrom(ProtoStreamReader reader) throws IOException {
        String key = reader.readString("key");
        String value = reader.readString("value");
        return new Something(key, value);
    }
}