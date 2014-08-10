/* Copyright (C) 2009 Business Management Systems, Inc.

This code is distributed under the GNU Library General Public License.

http://www.gnu.org/copyleft/lgpl.html

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Library General Public
License as published by the Free Software Foundation; either
version 2 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Library General Public License for more details.

You should have received a copy of the GNU Library General Public
License along with this library; if not, write to the
Free Software Foundation, Inc., 59 Temple Place - Suite 330,
Boston, MA  02111-1307, USA.

 * $Log: Base64CharsetProvider.java,v $
 * Revision 1.2  2009/04/29 00:50:44  stuart
 * More docs on the StreamDecoder bug.
 *
 * Revision 1.1  2009/04/28 22:01:26  stuart
 * Base64 coding via a Charset.
 *
 */
package bmsi.util;

import java.nio.charset.spi.*;
import java.nio.charset.*;
import java.nio.*;
import java.io.*;
import java.util.*;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.framework.Test;

/** Base64 encode/decode wrapped as a CharsetProvider.  This allows you to
  use the standard String ctor and getBytes() method with the
  "base64" encoding to encode and decode.  
  <p>
  You would also be able to use
  InputStreamReader and OutputStreamWriter to encode and decode.  
  Unfortunately, the Sun Java library has a
  <a href="http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4744247">
  bug</a> in sun.nio.cs.StreamDecoder
  (still present in JDK7) where it calls reset instead of flush at
  eof (
  <a href="http://www.docjar.com/docs/api/sun/nio/cs/StreamDecoder.html">
  see</a> the implRead method).  This means that until that is fixed,
  InputStreamReader will often be missing the last byte of input (i.e. when the
  number of input bytes is not an exact multiple of 3).  The bug has
  been "in progress" since 2003, so don't hold your breath.
  <p>
  See the instructions for {@link java.nio.charset.spi.CharsetProvider}
  on how to register this CharsetProvider.  In brief, 
  <code>META-INF/services/java.nio.charset.spi.CharsetProvider</code>
  is a magic file listing CharsetProvider classes in the same directory
  or jar, one per line.

  @author Stuart D. Gathman
    Copyright (C) 2009  Business Management Systems, Inc.
 */

public class Base64CharsetProvider extends CharsetProvider {
  private Map<String,Charset> dir = new HashMap<String,Charset>(10);

  /** The Charset implementations we provide. */
  private Charset[] charsets = new Charset[] {
    new Base64Charset("BASE64",new String[] {
     "base64", "BASE-64", "base-64" },0),
    new Base64Charset("BASE64LN",new String[] {
     "base64ln", "BASE-64LN", "base-64ln" },72)
  };

  public Base64CharsetProvider() {
    for (Charset cs: charsets) {
      dir.put(cs.name(),cs);
      for (String a: cs.aliases())
	dir.put(a,cs);
    }
  }

  public Charset charsetForName(String charsetName) {
    return dir.get(charsetName);
  }

  public Iterator<Charset> charsets() {
    return Arrays.asList(charsets).iterator();
  }

  private static String codeset =
	"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
  private static char[]    map1 = codeset.toCharArray();
  private static byte[]    map2 = new byte[128];
  static {
    Arrays.fill(map2,(byte)-1);
    for (int i=0; i<64; i++) map2[map1[i]] = (byte)i;
  }

  /** A base64 coder as a Charset.  The char[] form
    is the base64 encoding of the byte[] form.  This is the
    opposite meaning of "encode" and "decode" from most "encodings".
   */
  public class Base64Charset extends Charset {
    private int lnbrk;

    /** Create a new base64 coder.
      @param name	the canonical name
      @param aliases	aliases
      @param lnbrk	the maximum line length, or 0 for no line breaks
     */
    public Base64Charset(String name,String[] aliases,int lnbrk) {
      super(name,aliases);
      this.lnbrk = lnbrk;
    }

    public boolean contains(Charset c) { return equals(c); }

    public CharsetDecoder newDecoder() {
      /* The sense of "encode" and "decode" are reversed between
	base64 and unicode codings. */
      return new Base64Encoder(this,lnbrk);
    }

    public CharsetEncoder newEncoder() {
      return new Base64Decoder(this);
    }
    
  }

  /** The sense of "encode" and "decode" are reversed between
    base64 and unicode codings. */
  static class Base64Decoder extends CharsetEncoder {
    Base64Decoder(Charset cs) { super(cs,0.75f,1.0f); }
    private int word = 0;
    private int cnt = 0;
    protected CoderResult encodeLoop(CharBuffer in, ByteBuffer out) {
      int word = this.word;
      int cnt = this.cnt;
      try {
	for (;;) {
	  if (cnt >= 8) {
	    byte x = (byte)(word >> (cnt - 8));
	    out.put(x);
	    cnt -= 8;
	  }
	  char c = in.get();
	  if (c == '=') {
	    cnt += 6;
	    if (cnt >= 8) cnt -= 8;
	    continue;
	  }
	  if (Character.isWhitespace(c)) continue;
	  if (c > 127) return CoderResult.unmappableForLength(1);
	  int i = map2[c];
	  if (i < 0) return CoderResult.unmappableForLength(1);
	  word = (word << 6) | i;
	  cnt += 6;
	}
      }
      catch (BufferUnderflowException x) { return CoderResult.UNDERFLOW; }
      catch (BufferOverflowException x) { return CoderResult.OVERFLOW; }
      finally {
	this.word = word;
	this.cnt = cnt;
      }
    }

    protected CoderResult implFlush(ByteBuffer out) {
      if (cnt > 0)
        return CoderResult.malformedForLength(4 - cnt/2);
      return CoderResult.UNDERFLOW;
    }

    protected void implReset() {
      cnt = 0;
    }

    public boolean canEncode(char c) {
      return (c < 128 && map2[c] >= 0);
    }

    public boolean isLegalReplacement(byte[] repl) { return true; }
  }

  /** The sense of "encode" and "decode" are reversed between
    base64 and unicode codings. */
  static class Base64Encoder extends CharsetDecoder {
    private int word = 0;
    private int cnt = 0;
    private int lnlen = 0;
    private int lnmax = 0;
    Base64Encoder(Charset cs,int lnmax) {
      super(cs,4.0f/3f,4.0f);
      this.lnmax = lnmax;
      //onUnmappableCharacter(CodingErrorAction.REPORT);
    }
    protected CoderResult decodeLoop(ByteBuffer in, CharBuffer out) {
      int word = this.word;
      int cnt = this.cnt;
      int lnlen = this.lnlen;
      try {
	for (;;) {
	  if (lnmax > 0 && lnlen >= lnmax) {
	    out.put('\n');
	    lnlen = 0;
	  }
	  while (cnt >= 6) {
	    char c = map1[(word >> (cnt - 6)) & 63];
	    out.put(c);
	    cnt -= 6;
	    ++lnlen;
	  }
	  byte x = in.get();
	  word = (word << 8) | (x & 255);
	  cnt += 8;
	}
      }
      catch (BufferUnderflowException x) { return CoderResult.UNDERFLOW; }
      catch (BufferOverflowException x) { return CoderResult.OVERFLOW; }
      finally {
	this.word = word;
	this.cnt = cnt;
	this.lnlen = lnlen;
      }
    }

    /** This is called at the end of input.  Unfortunately, Sun's
    StreamDecoder, and therefore InputStreamReader, are broken, and
    call reset instead - which does not let us output the last char
    or two.
     */
    protected CoderResult implFlush(CharBuffer out) {
      int word = this.word;
      //System.err.printf("cnt=%d\n",cnt);
      try {
	if (lnmax > 0 && lnlen >= lnmax) {
	  out.put('\n');
	  lnlen = 0;
	}
	if (cnt > 0 && cnt < 6) {
	  word <<= 8;
	  char c = map1[(word >> (cnt + 2)) & 63];
	  out.put(c);
	  cnt += 2;
	  if (cnt < 6)
	    cnt += 8;
	  ++lnlen;
	}
	while (cnt >= 6) {
	  out.put('=');
	  cnt -= 6;
	  ++lnlen;
	}
	if (lnmax > 0 && lnlen > 0) {
	  out.put('\n');
	  lnlen = 0;
	}
      }
      catch (BufferOverflowException x) { return CoderResult.OVERFLOW; }
      return CoderResult.UNDERFLOW;
    }

    protected void implReset() {
      cnt = 0;
      lnlen = 0;
    }
  }

  /** Test cases for the base64 Charset. */
  public static class Test extends TestCase {
    public Test(String name) { super(name); }
    private static byte[] bin = new byte[] {
      (byte)0x69, (byte)0xb7, (byte)0x1d, (byte)0x79
    };
    private static byte[] bin2 = new byte[] {
      (byte)0x69, (byte)0xb7, (byte)0x1d, (byte)0x79, (byte)0xf8
    };

    public void testEncode() throws Exception {
      String s = "abcdef";
      byte[] b = s.getBytes();
      String b64 = new String(b,"base64");
      assertEquals("YWJjZGVm",b64);
      assertTrue(Arrays.equals(b64.getBytes("base64"),b));
      s = new String(bin,"base64");
      assertEquals("abcdeQ==",s);
      s = new String(bin2,"base64");
      assertEquals("abcdefg=",s);
    }

    public void testInputStream() throws Exception {
      if (true) return;
      /* There seems to be a buf in InputStreamReader, it never
         calls CharsetDecoder.flush().  In Java 7, 
	 sun.nio.cs.StreamDecoder.java calls decoder.reset() instead of
	 decoder.flush() when eof is detected. */
      ByteArrayInputStream is = new ByteArrayInputStream(bin2);
      Reader r = new InputStreamReader(is,"base64ln");
      BufferedReader br = new BufferedReader(r);
      System.err.print("inputStream: ");
      String ln = br.readLine();
      assertEquals("abcdefg=",ln);
      ln = br.readLine();
      assertTrue(ln == null);
    }

    public void testDecode() throws Exception {
      String s = "abcdef==";
      byte[] b = s.getBytes("base64");
      assertTrue(Arrays.equals(bin,b));
    }

    public void testMalformed() throws Exception {
      String s = "abcdef";
      try {
	byte[] b = s.getBytes("base64");
	fail("malformed base64 string accepted");
      }
      catch (Error x) {
        assertTrue(x.getCause() instanceof MalformedInputException);
      }
      s = "abcdef!!!";
      try {
	byte[] b = s.getBytes("base64");
	/* FIXME: The javadocs actually state that the behaviour with 
	 unmappable chars is undefined, and to use the CharsetEncoder
	 instead for more precise control. */
	fail("illegal base64 chars accepted");
      }
      catch (IllegalArgumentException x) { }
    }
  }
}
