package com.fasterxml.jackson.module.afterburner.ser;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.impl.PropertySerializerMap;

public class ObjectMethodPropertyWriter
    extends OptimizedBeanPropertyWriter<ObjectMethodPropertyWriter>
{
    public ObjectMethodPropertyWriter(BeanPropertyWriter src, BeanPropertyAccessor acc, int index,
            JsonSerializer<Object> ser) {
        super(src, acc, index, ser);
    }

    @Override
    public BeanPropertyWriter withSerializer(JsonSerializer<Object> ser) {
        return new ObjectMethodPropertyWriter(this, _propertyAccessor, _propertyIndex, ser);
    }

    @Override
    public ObjectMethodPropertyWriter withAccessor(BeanPropertyAccessor acc) {
        if (acc == null) throw new IllegalArgumentException();
        return new ObjectMethodPropertyWriter(this, acc, _propertyIndex, _serializer);
    }

    /*
    /**********************************************************
    /* Overrides
    /**********************************************************
     */

    @Override
    public final void serializeAsField(Object bean, JsonGenerator jgen, SerializerProvider prov) throws Exception
    {
        if (broken) {
            fallbackWriter.serializeAsField(bean, jgen, prov);
            return;
        }
        try {
            Object value = _propertyAccessor.objectGetter(bean, _propertyIndex);
            // Null (etc) handling; copied from super-class impl
            if (value == null) {
                if (_nullSerializer != null) {
                    jgen.writeFieldName(_fastName);
                    _nullSerializer.serialize(null, jgen, prov);
                } else if (!_suppressNulls) {
                    jgen.writeFieldName(_fastName);
                    prov.defaultSerializeNull(jgen);
                }
                return;
            }
            JsonSerializer<Object> ser = _serializer;
            if (ser == null) {
                Class<?> cls = value.getClass();
                PropertySerializerMap map = _dynamicSerializers;
                ser = map.serializerFor(cls);
                if (ser == null) {
                    ser = _findAndAddDynamic(map, cls, prov);
                }
            }
            if (_suppressableValue != null) {
                if (MARKER_FOR_EMPTY == _suppressableValue) {
                    if (ser.isEmpty(value)) {
                        return;
                    }
                } else if (_suppressableValue.equals(value)) {
                    return;
                }
            }
            if (value == bean) {
                _handleSelfReference(bean, jgen, prov, ser);
            }
            jgen.writeFieldName(_fastName);
            if (_typeSerializer == null) {
                ser.serialize(value, jgen, prov);
            } else {
                ser.serializeWithType(value, jgen, prov, _typeSerializer);
            }
        } catch (IllegalAccessError e) {
            _reportProblem(bean, e);
            fallbackWriter.serializeAsField(bean, jgen, prov);
        } catch (SecurityException e) {
            _reportProblem(bean, e);
            fallbackWriter.serializeAsField(bean, jgen, prov);
        }
    }

    @Override
    public final void serializeAsElement(Object bean, JsonGenerator jgen, SerializerProvider prov) throws Exception
    {
        if (!broken) {
            try {
                Object value = _propertyAccessor.objectGetter(bean, _propertyIndex);
                // Null (etc) handling; copied from super-class impl
                if (value == null) {
                    if (_nullSerializer != null) {
                        _nullSerializer.serialize(null, jgen, prov);
                    } else if (_suppressNulls) {
                        serializeAsPlaceholder(bean, jgen, prov);
                    } else {
                        prov.defaultSerializeNull(jgen);
                    }
                    return;
                }
                JsonSerializer<Object> ser = _serializer;
                if (ser == null) {
                    Class<?> cls = value.getClass();
                    PropertySerializerMap map = _dynamicSerializers;
                    ser = map.serializerFor(cls);
                    if (ser == null) {
                        ser = _findAndAddDynamic(map, cls, prov);
                    }
                }
                if (_suppressableValue != null) {
                    if (MARKER_FOR_EMPTY == _suppressableValue) {
                        if (ser.isEmpty(value)) {
                            serializeAsPlaceholder(bean, jgen, prov);
                            return;
                        }
                    } else if (_suppressableValue.equals(value)) {
                        serializeAsPlaceholder(bean, jgen, prov);
                        return;
                    }
                }
                if (value == bean) {
                    _handleSelfReference(bean, jgen, prov, ser);
                }
                if (_typeSerializer == null) {
                    ser.serialize(value, jgen, prov);
                } else {
                    ser.serializeWithType(value, jgen, prov, _typeSerializer);
                }
                return;
            } catch (IllegalAccessError e) {
                _reportProblem(bean, e);
            } catch (SecurityException e) {
                _reportProblem(bean, e);
            }
        }
        fallbackWriter.serializeAsElement(bean, jgen, prov);
    }
}
