package retrofit3;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Map;

import okhttp3.Headers;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

import static retrofit3.Utils.checkNotNull;
/**
 * Created by allen on 16-8-28.
 */
abstract class ParameterHandler<T>{
    abstract void apply(RequestBuilder builder,T values) throws IOException;

    final ParameterHandler<Iterable<T>>iterable(){
        return new ParameterHandler<Iterable<T>>() {
            @Override
            void apply(RequestBuilder builder, Iterable<T> values) throws IOException {
                if(values==null) return; //Skip null values

                for(T value:values){
                    ParameterHandler.this.apply(builder,value);
                }
            }
        };
    }

    final ParameterHandler<Object>array(){
       return new ParameterHandler<Object>() {
           @Override
           void apply(RequestBuilder builder, Object values) throws IOException {
               if(values==null) return; //Skip null values

               for(int i=0,size= Array.getLength(values);i<size;i++){
                   //noinspection unchecked
                   ParameterHandler.this.apply(builder,(T)Array.get(values,i));
               }
           }
       };
    }

    static final class RelativeUrl extends ParameterHandler<Object>{
        @Override
        void apply(RequestBuilder builder, Object values) throws IOException {
            builder.setRelativeUrl(values);
        }
    }

    static final class Header<T> extends ParameterHandler<T>{
        private final String name;
        private final Converter<T,String>valueConverter;

        Header(String name,Converter<T,String>valueConverter){
            this.name=checkNotNull(name,"name==null");
            this.valueConverter=valueConverter;
        }

        @Override void apply(RequestBuilder builder,T value) throws IOException {
            if(value==null) return;
            builder.addHeader(name,valueConverter.convert(value));
        }
    }

    static final class Path<T> extends ParameterHandler<T>{
        private final String name;
        private final Converter<T,String>valueConverter;
        private final boolean encoded;

        Path(String name,Converter<T,String>valueConverter,boolean encoded){
            this.name=checkNotNull(name,"name==null");
            this.valueConverter=valueConverter;
            this.encoded=encoded;
        }

        @Override
        void apply(RequestBuilder builder, T value) throws IOException {
            if(null==value){
                throw new IllegalArgumentException("Path parameter \""+name+"\" value must not be null");
            }
            builder.addPathParam(name,valueConverter.convert(value),encoded);
        }
    }

    static final class Query<T> extends ParameterHandler<T>{
        private final String name;
        private final Converter<T,String>valueConverter;
        private final boolean encoded;

        Query(String name,Converter<T,String>valueConverter,boolean encoded){
            this.name=checkNotNull(name,"name == null");
            this.valueConverter=valueConverter;
            this.encoded=encoded;
        }

        @Override void apply(RequestBuilder builder, T value) throws IOException {
            if(value==null) return;
            builder.addQueryParam(name,valueConverter.convert(value),encoded);
        }
    }

    static final class QueryMap<T> extends ParameterHandler<Map<String,T>>{
        private final Converter<T,String>valueConverter;
        private final boolean encoded;

        QueryMap(Converter<T,String>valueConverter,boolean encoded){
            this.valueConverter=valueConverter;
            this.encoded=encoded;
        }

        @Override void apply(RequestBuilder builder,Map<String,T>value) throws IOException {
            if(value ==null){
                throw new IllegalArgumentException("Query map was null");
            }

            for(Map.Entry<String,T>entry:value.entrySet()){
                String entryKey=entry.getKey();
                if(entryKey==null){
                    throw new IllegalArgumentException("Query map contained null key.");
                }
                T entryValue=entry.getValue();
                if(entryValue==null){
                    throw new IllegalArgumentException("Query map contained null value for key '"+entryKey+"'.");
                }
                builder.addQueryParam(entryKey,valueConverter.convert(entryValue),encoded);
            }
        }
    }

    static final class Field<T> extends ParameterHandler<T> {
        private final String name;
        private final Converter<T,String>valueConverter;
        private final boolean encoded;

        Field(String name,Converter<T,String>valueConverter,boolean encoded){
            this.name=name;
            this.valueConverter=valueConverter;
            this.encoded=encoded;
        }

        @Override void apply(RequestBuilder builder,T value) throws IOException{
            if(value==null) return;
            builder.addFormField(name,valueConverter.convert(value),encoded);
        }
    }

    static final class FieldMap<T> extends ParameterHandler<Map<String, T>> {
        private final Converter<T, String> valueConverter;
        private final boolean encoded;

        FieldMap(Converter<T, String> valueConverter, boolean encoded) {
            this.valueConverter = valueConverter;
            this.encoded = encoded;
        }

        @Override void apply(RequestBuilder builder, Map<String, T> value) throws IOException {
            if (value == null) {
                throw new IllegalArgumentException("Field map was null.");
            }

            for (Map.Entry<String, T> entry : value.entrySet()) {
                String entryKey = entry.getKey();
                if (entryKey == null) {
                    throw new IllegalArgumentException("Field map contained null key.");
                }
                T entryValue = entry.getValue();
                if (entryValue == null) {
                    throw new IllegalArgumentException(
                            "Field map contained null value for key '" + entryKey + "'.");
                }
                builder.addFormField(entryKey, valueConverter.convert(entryValue), encoded);
            }
        }
    }

    static final class Part<T> extends ParameterHandler<T> {
        private final Headers headers;
        private final Converter<T, RequestBody>converter;

        Part(Headers headers,Converter<T,RequestBody>converter){
            this.headers=headers;
            this.converter=converter;
        }

        @Override void apply(RequestBuilder builder,T value){
            if(null==value) return;

            RequestBody body;
            try{
                body=converter.convert(value);
            }catch(IOException e){
                throw new RuntimeException("Unable to convert "+value+" to RequestBody",e);
            }
            builder.addPart(headers,body);
        }
    }

    static final class RawPart extends ParameterHandler<MultipartBody.Part>{
        static final RawPart INSTANCE=new RawPart();

        private RawPart(){}

        @Override void apply(RequestBuilder builder,MultipartBody.Part value) throws IOException {
            if(value!=null){
                builder.addPart(value);
            }
        }
    }

    static final class PartMap<T> extends ParameterHandler<Map<String,T>>{
        private final Converter<T,RequestBody>valueConverter;
        private final String transferEncoding;

        PartMap(Converter<T,RequestBody>valueConverter,String transferEncoding){
            this.valueConverter=valueConverter;
            this.transferEncoding=transferEncoding;
        }

        @Override void apply(RequestBuilder builder,Map<String,T>value) throws IOException {
            if(value==null){
                throw new IllegalArgumentException("Part map was null");
            }

            for(Map.Entry<String,T> entry:value.entrySet()){
                String entryKey=entry.getKey();
                if(entryKey==null){
                    throw new IllegalArgumentException("Part map contained null key.");
                }
                T entryValue=entry.getValue();
                if(entryValue==null){
                    throw new IllegalArgumentException("Part map contained null value for key '"+entryKey+"'.");
                }
                Headers headers=Headers.of("Content-Disposition","form-data; name=\""+entryKey+"\"",
                        "Content-Transfer-Encoding",transferEncoding);
                builder.addPart(headers,valueConverter.convert(entryValue));
            }
        }
    }

    static final class Body<T> extends ParameterHandler<T> {
        private final Converter<T, RequestBody> converter;

        Body(Converter<T, RequestBody> converter) {
            this.converter = converter;
        }

        @Override void apply(RequestBuilder builder, T value) {
            if (value == null) {
                throw new IllegalArgumentException("Body parameter value must not be null.");
            }
            RequestBody body;
            try {
                body = converter.convert(value);
            } catch (IOException e) {
                throw new RuntimeException("Unable to convert " + value + " to RequestBody", e);
            }
            builder.setBody(body);
        }
    }

}
