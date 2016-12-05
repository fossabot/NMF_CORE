package esa.mo.mal.encoder.tcpip;

import java.io.InputStream;
import java.util.List;
import java.util.logging.Level;

import org.ccsds.moims.mo.mal.MALException;
import org.ccsds.moims.mo.mal.structures.Blob;
import org.ccsds.moims.mo.mal.structures.Identifier;
import org.ccsds.moims.mo.mal.structures.UInteger;

import esa.mo.mal.encoder.binary.fixed.FixedBinaryDecoder;
import static esa.mo.mal.transport.tcpip.TCPIPTransport.RLOGGER;

/**
 * TCPIP Header decoder
 * @author Rian van Gijlswijk <r.vangijlswijk@telespazio-vega.de>
 *
 */
public class TCPIPFixedBinaryDecoder extends FixedBinaryDecoder {
	
	protected TCPIPFixedBinaryDecoder(java.io.InputStream is) {
		super(new TCPIPBufferHolder(is, null, 0, 0));
	}
	
	public TCPIPFixedBinaryDecoder(final BufferHolder srcBuffer) {
		super(srcBuffer);
	}
	
	public TCPIPFixedBinaryDecoder(byte[] buf, int offset) {
		super(new TCPIPBufferHolder(null, buf, offset, 0));
	}

	public org.ccsds.moims.mo.mal.MALListDecoder createListDecoder(final List list) throws MALException {
		return new TCPIPFixedBinaryListDecoder(list, sourceBuffer);
	}

	@Override
	public String decodeString() throws MALException {
		
	      return sourceBuffer.getString();
	}
	
	public Long decodeMALLong() throws MALException {
		
		return sourceBuffer.getSignedLong();
	}
	
	public UInteger decodeUInteger() throws MALException {
		
		return new UInteger(sourceBuffer.getUnsignedInt());
	}
	
	@Override
	public Identifier decodeNullableIdentifier() throws MALException {
		
		// decode presence flag
		boolean isNotNull = decodeBoolean();
		
		RLOGGER.finest("Decoding identifier. Is null: " + !isNotNull);
		
		// decode one element, or add null if presence flag indicates no element
		if (isNotNull) {
			return decodeIdentifier();
		}

		return null;
	}
	
	@Override
	public Integer decodeInteger() throws MALException {
		
		return ((TCPIPBufferHolder)sourceBuffer).get32();
	}

	@Override
	public Blob decodeBlob() throws MALException {
		
		int sz = (int)decodeUInteger().getValue();
		
		if (sz == 0) {
			return null;
		}
		
		return new Blob(sourceBuffer.directGetBytes(sz));
	}
	
	public int getBufferOffset() {
		return ((TCPIPBufferHolder)this.sourceBuffer).getOffset();
	}
	
	public BufferHolder getBuffer() {
		return this.sourceBuffer;
	}
	
	/**
	 * Internal class that implements the fixed length field decoding.
	 */
	protected static class TCPIPBufferHolder extends FixedBufferHolder {

		public TCPIPBufferHolder(InputStream is, byte[] buf, int offset, int length) {
			super(is, buf, offset, length);
		}

		@Override
		public String getString() throws MALException {
			
			final long len = getUnsignedInt();
			String logString = "Decode string: length " + len;

			if (len > Integer.MAX_VALUE) {
				throw new MALException("Value is too big to decode! Please provide a string with a length lower than INT_MAX");
			}
			
			if (len >= 0) {
				buf.checkBuffer((int) len);

				final String s = new String(buf.getBuf(), buf.getOffset(), (int) len, UTF8_CHARSET);
				buf.shiftOffsetAndReturnPrevious((int)len);
				logString += " val " + s;
				return s;
			}
			RLOGGER.log(Level.FINEST, logString);
			
			return null;
		}
		
		/**
		 * Decode an unsigned int using a split-binary approach
		 */
		@Override
		public int getUnsignedInt() throws MALException {
			
			int value = 0;
			int i = 0;
			int b;
			while (((b = get8()) & 0x80) != 0) {
				value |= (b & 0x7F) << i;
				i += 7;
			}
			return value | (b << i);
		}
		
		public int get32() throws MALException {
			
			buf.checkBuffer(4);

			final int i = buf.shiftOffsetAndReturnPrevious(4);
			return java.nio.ByteBuffer.wrap(buf.getBuf(), i, 4).getInt() & 0xFFFFFFF;
		}
		
		public int getOffset() {
			return buf.getOffset();
		}
	}
}