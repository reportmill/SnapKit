package snap.web;
import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.PosixFilePermission;
import java.util.*;

/**
 * The JTar library.
 * Copyright 2012 Kamran Zafar 
 * From: https://github.com/kamranzafar/jtar
 */
public class JTar {

/**
 * Constants.
 */
public static class TarConstants {
    public static final int EOF_BLOCK = 1024;
    public static final int DATA_BLOCK = 512;
    public static final int HEADER_BLOCK = 512;
}

/**
 * A class to represent file information in a Tar file.
 */
public static class TarEntry {
    protected File file;
    protected TarHeader header;

    private TarEntry() { this.file = null; header = new TarHeader(); }

    public TarEntry(File file, String entryName) { this(); this.file = file; this.extractTarHeader(entryName); }

    public TarEntry(byte[] headerBuf) { this(); this.parseTarHeader(headerBuf); }

    /** Constructor to create an entry from an existing TarHeader object.  */
    public TarEntry(TarHeader header) { this.file = null; this.header = header; }

    public boolean equals(TarEntry it) { return header.name.toString().equals(it.header.name.toString()); }

    public boolean isDescendent(TarEntry desc) { return desc.header.name.toString().startsWith(header.name.toString());}

    public TarHeader getHeader() { return header; }

    public String getName()
    {
        String name = header.name.toString();
        if (header.namePrefix != null && !header.namePrefix.toString().equals("")) {
            name = header.namePrefix.toString() + "/" + name;
        }

        return name;
    }

    public void setName(String name) { header.name = new StringBuffer(name); }

    public int getUserId() { return header.userId; }

    public void setUserId(int userId) { header.userId = userId; }

    public int getGroupId() { return header.groupId; }

    public void setGroupId(int groupId) { header.groupId = groupId; }

    public String getUserName() { return header.userName.toString(); }

    public void setUserName(String userName) { header.userName = new StringBuffer(userName); }

    public String getGroupName() { return header.groupName.toString(); }

    public void setGroupName(String groupName) { header.groupName = new StringBuffer(groupName); }

    public void setIds(int userId, int groupId) { this.setUserId(userId); this.setGroupId(groupId); }

    public void setModTime(long time) { header.modTime = time / 1000; }

    public void setModTime(Date time) { header.modTime = time.getTime() / 1000; }

    public Date getModTime() { return new Date(header.modTime * 1000); }

    public File getFile() { return this.file; }

    public long getSize() { return header.size; }

    public void setSize(long size) { header.size = size; }

    /**
     * Checks if the org.kamrazafar.jtar entry is a directory
     */
    public boolean isDirectory() {
        if (this.file != null)
            return this.file.isDirectory();

        if (header != null) {
            if (header.linkFlag == TarHeader.LF_DIR)
                return true;

            if (header.name.toString().endsWith("/"))
                return true;
        }

        return false;
    }

    /**
     * Extract header from File
     */
    public void extractTarHeader(String entryName) {
        int permissions = PermissionUtils.permissions(file);
        header = TarHeader.createHeader(entryName, file.length(), file.lastModified() / 1000, file.isDirectory(), permissions);
    }

    /**
     * Calculate checksum
     */
    public long computeCheckSum(byte[] buf) {
        long sum = 0;

        for (int i = 0; i < buf.length; ++i) {
            sum += 255 & buf[i];
        }

        return sum;
    }

    /**
     * Writes the header to the byte buffer
     */
    public void writeEntryHeader(byte[] outbuf) {
        int offset = 0;

        offset = TarHeader.getNameBytes(header.name, outbuf, offset, TarHeader.NAMELEN);
        offset = Octal.getOctalBytes(header.mode, outbuf, offset, TarHeader.MODELEN);
        offset = Octal.getOctalBytes(header.userId, outbuf, offset, TarHeader.UIDLEN);
        offset = Octal.getOctalBytes(header.groupId, outbuf, offset, TarHeader.GIDLEN);

        long size = header.size;

        offset = Octal.getLongOctalBytes(size, outbuf, offset, TarHeader.SIZELEN);
        offset = Octal.getLongOctalBytes(header.modTime, outbuf, offset, TarHeader.MODTIMELEN);

        int csOffset = offset;
        for (int c = 0; c < TarHeader.CHKSUMLEN; ++c)
            outbuf[offset++] = (byte) ' ';

        outbuf[offset++] = header.linkFlag;

        offset = TarHeader.getNameBytes(header.linkName, outbuf, offset, TarHeader.NAMELEN);
        offset = TarHeader.getNameBytes(header.magic, outbuf, offset, TarHeader.USTAR_MAGICLEN);
        offset = TarHeader.getNameBytes(header.userName, outbuf, offset, TarHeader.USTAR_USER_NAMELEN);
        offset = TarHeader.getNameBytes(header.groupName, outbuf, offset, TarHeader.USTAR_GROUP_NAMELEN);
        offset = Octal.getOctalBytes(header.devMajor, outbuf, offset, TarHeader.USTAR_DEVLEN);
        offset = Octal.getOctalBytes(header.devMinor, outbuf, offset, TarHeader.USTAR_DEVLEN);
        offset = TarHeader.getNameBytes(header.namePrefix, outbuf, offset, TarHeader.USTAR_FILENAME_PREFIX);

        for (; offset < outbuf.length;)
            outbuf[offset++] = 0;

        long checkSum = this.computeCheckSum(outbuf);

        Octal.getCheckSumOctalBytes(checkSum, outbuf, csOffset, TarHeader.CHKSUMLEN);
    }

    /**
     * Parses the tar header to the byte buffer
     * 
     * @param header
     * @param bh
     */
    public void parseTarHeader(byte[] bh) {
        int offset = 0;

        header.name = TarHeader.parseName(bh, offset, TarHeader.NAMELEN);
        offset += TarHeader.NAMELEN;

        header.mode = (int) Octal.parseOctal(bh, offset, TarHeader.MODELEN);
        offset += TarHeader.MODELEN;

        header.userId = (int) Octal.parseOctal(bh, offset, TarHeader.UIDLEN);
        offset += TarHeader.UIDLEN;

        header.groupId = (int) Octal.parseOctal(bh, offset, TarHeader.GIDLEN);
        offset += TarHeader.GIDLEN;

        header.size = Octal.parseOctal(bh, offset, TarHeader.SIZELEN);
        offset += TarHeader.SIZELEN;

        header.modTime = Octal.parseOctal(bh, offset, TarHeader.MODTIMELEN);
        offset += TarHeader.MODTIMELEN;

        header.checkSum = (int) Octal.parseOctal(bh, offset, TarHeader.CHKSUMLEN);
        offset += TarHeader.CHKSUMLEN;

        header.linkFlag = bh[offset++];

        header.linkName = TarHeader.parseName(bh, offset, TarHeader.NAMELEN);
        offset += TarHeader.NAMELEN;

        header.magic = TarHeader.parseName(bh, offset, TarHeader.USTAR_MAGICLEN);
        offset += TarHeader.USTAR_MAGICLEN;

        header.userName = TarHeader.parseName(bh, offset, TarHeader.USTAR_USER_NAMELEN);
        offset += TarHeader.USTAR_USER_NAMELEN;

        header.groupName = TarHeader.parseName(bh, offset, TarHeader.USTAR_GROUP_NAMELEN);
        offset += TarHeader.USTAR_GROUP_NAMELEN;

        header.devMajor = (int) Octal.parseOctal(bh, offset, TarHeader.USTAR_DEVLEN);
        offset += TarHeader.USTAR_DEVLEN;

        header.devMinor = (int) Octal.parseOctal(bh, offset, TarHeader.USTAR_DEVLEN);
        offset += TarHeader.USTAR_DEVLEN;

        header.namePrefix = TarHeader.parseName(bh, offset, TarHeader.USTAR_FILENAME_PREFIX);
    }
}

/**
 * Header
 * 
 * <pre>
 * Offset  Size     Field
 * 0       100      File name
 * 100     8        File mode
 * 108     8        Owner's numeric user ID
 * 116     8        Group's numeric user ID
 * 124     12       File size in bytes
 * 136     12       Last modification time in numeric Unix time format
 * 148     8        Checksum for header block
 * 156     1        Link indicator (file type)
 * 157     100      Name of linked file
 * </pre>
 * 
 * File Types
 * 
 * <pre>
 * Value        Meaning
 * '0'          Normal file
 * (ASCII NUL)  Normal file (now obsolete)
 * '1'          Hard link
 * '2'          Symbolic link
 * '3'          Character special
 * '4'          Block special
 * '5'          Directory
 * '6'          FIFO
 * '7'          Contigous
 * </pre>
 * 
 * Ustar header
 * 
 * <pre>
 * Offset  Size    Field
 * 257     6       UStar indicator "ustar"
 * 263     2       UStar version "00"
 * 265     32      Owner user name
 * 297     32      Owner group name
 * 329     8       Device major number
 * 337     8       Device minor number
 * 345     155     Filename prefix
 * </pre>
 */

public static class TarHeader {

    /* Header */
    public static final int NAMELEN = 100;
    public static final int MODELEN = 8;
    public static final int UIDLEN = 8;
    public static final int GIDLEN = 8;
    public static final int SIZELEN = 12;
    public static final int MODTIMELEN = 12;
    public static final int CHKSUMLEN = 8;
    public static final byte LF_OLDNORM = 0;

    /* File Types */
    public static final byte LF_NORMAL = (byte) '0';
    public static final byte LF_LINK = (byte) '1';
    public static final byte LF_SYMLINK = (byte) '2';
    public static final byte LF_CHR = (byte) '3';
    public static final byte LF_BLK = (byte) '4';
    public static final byte LF_DIR = (byte) '5';
    public static final byte LF_FIFO = (byte) '6';
    public static final byte LF_CONTIG = (byte) '7';

    /* Ustar header */

    public static final String USTAR_MAGIC = "ustar"; // POSIX

    public static final int USTAR_MAGICLEN = 8;
    public static final int USTAR_USER_NAMELEN = 32;
    public static final int USTAR_GROUP_NAMELEN = 32;
    public static final int USTAR_DEVLEN = 8;
    public static final int USTAR_FILENAME_PREFIX = 155;

    // Header values
    public StringBuffer name;
    public int mode;
    public int userId;
    public int groupId;
    public long size;
    public long modTime;
    public int checkSum;
    public byte linkFlag;
    public StringBuffer linkName;
    public StringBuffer magic; // ustar indicator and version
    public StringBuffer userName;
    public StringBuffer groupName;
    public int devMajor;
    public int devMinor;
    public StringBuffer namePrefix;

    public TarHeader() {
        this.magic = new StringBuffer(TarHeader.USTAR_MAGIC);

        this.name = new StringBuffer();
        this.linkName = new StringBuffer();

        String user = System.getProperty("user.name", "");

        if (user.length() > 31)
            user = user.substring(0, 31);

        this.userId = 0;
        this.groupId = 0;
        this.userName = new StringBuffer(user);
        this.groupName = new StringBuffer("");
        this.namePrefix = new StringBuffer();
    }

    /**
     * Parse an entry name from a header buffer.
     */
    public static StringBuffer parseName(byte[] header, int offset, int length) {
        StringBuffer result = new StringBuffer(length);

        int end = offset + length;
        for (int i = offset; i < end; ++i) {
            if (header[i] == 0)
                break;
            result.append((char) header[i]);
        }

        return result;
    }

    /**
     * Determine the number of bytes in an entry name.
     */
    public static int getNameBytes(StringBuffer name, byte[] buf, int offset, int length) {
        int i;

        for (i = 0; i < length && i < name.length(); ++i) {
            buf[offset + i] = (byte) name.charAt(i);
        }

        for (; i < length; ++i) {
            buf[offset + i] = 0;
        }

        return offset + length;
    }

    /**
     * Creates a new header for a file/directory entry.
     */
    public static TarHeader createHeader(String entryName, long size, long modTime, boolean dir, int permissions) {
        String name = entryName;
        name = TarUtils.trim(name.replace(File.separatorChar, '/'), '/');

        TarHeader header = new TarHeader();
        header.linkName = new StringBuffer("");
        header.mode = permissions;

        if (name.length() > 100) {
            header.namePrefix = new StringBuffer(name.substring(0, name.lastIndexOf('/')));
            header.name = new StringBuffer(name.substring(name.lastIndexOf('/') + 1));
        } else {
            header.name = new StringBuffer(name);
        }
        if (dir) {
            header.linkFlag = TarHeader.LF_DIR;
            if (header.name.charAt(header.name.length() - 1) != '/') {
                header.name.append("/");
            }
            header.size = 0;
        } else {
            header.linkFlag = TarHeader.LF_NORMAL;
            header.size = size;
        }

        header.modTime = modTime;
        header.checkSum = 0;
        header.devMajor = 0;
        header.devMinor = 0;

        return header;
    }
}

public class TarInputStream extends FilterInputStream {

    private static final int SKIP_BUFFER_SIZE = 2048;
    private TarEntry currentEntry;
    private long currentFileSize;
    private long bytesRead;
    private boolean defaultSkip = false;

    public TarInputStream(InputStream in)  { super(in); currentFileSize = 0; bytesRead = 0; }

    @Override
    public boolean markSupported() { return false; }

    /** Not supported */
    @Override
    public synchronized void mark(int readlimit) { }

    /** Not supported */
    @Override
    public synchronized void reset() throws IOException { throw new IOException("mark/reset not supported"); }

    /**
     * Read a byte
     */
    @Override
    public int read() throws IOException {
        byte[] buf = new byte[1];
        int res = this.read(buf, 0, 1);
        if (res != -1) {
            return 0xFF & buf[0];
        }
        return res;
    }

    /**
     * Checks if the bytes being read exceed the entry size and adjusts the byte array length. Updates the byte counters
     */
    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (currentEntry != null) {
            if (currentFileSize == currentEntry.getSize()) {
                return -1;
            } else if ((currentEntry.getSize() - currentFileSize) < len) {
                len = (int) (currentEntry.getSize() - currentFileSize);
            }
        }

        int br = super.read(b, off, len);

        if (br != -1) {
            if (currentEntry != null) {
                currentFileSize += br;
            }

            bytesRead += br;
        }

        return br;
    }

    /**
     * Returns the next entry in the tar file
     */
    public TarEntry getNextEntry() throws IOException {
        closeCurrentEntry();

        byte[] header = new byte[TarConstants.HEADER_BLOCK];
        byte[] theader = new byte[TarConstants.HEADER_BLOCK];
        int tr = 0;

        // Read full header
        while (tr < TarConstants.HEADER_BLOCK) {
            int res = read(theader, 0, TarConstants.HEADER_BLOCK - tr);

            if (res < 0) {
                break;
            }

            System.arraycopy(theader, 0, header, tr, res);
            tr += res;
        }

        // Check if record is null
        boolean eof = true;
        for (byte b : header) {
            if (b != 0) {
                eof = false;
                break;
            }
        }

        if (!eof) {
            currentEntry = new TarEntry(header);
        }

        return currentEntry;
    }

    /**
     * Returns the current offset (in bytes) from the beginning of the stream. 
     * This can be used to find out at which point in a tar file an entry's content begins, for instance. 
     */
    public long getCurrentOffset() { return bytesRead; }
    
    /**
     * Closes the current tar entry
     */
    protected void closeCurrentEntry() throws IOException {
        if (currentEntry != null) {
            if (currentEntry.getSize() > currentFileSize) {
                // Not fully read, skip rest of the bytes
                long bs = 0;
                while (bs < currentEntry.getSize() - currentFileSize) {
                    long res = skip(currentEntry.getSize() - currentFileSize - bs);

                    if (res == 0 && currentEntry.getSize() - currentFileSize > 0) {
                        // I suspect file corruption
                        throw new IOException("Possible tar file corruption");
                    }

                    bs += res;
                }
            }

            currentEntry = null;
            currentFileSize = 0L;
            skipPad();
        }
    }

    /**
     * Skips the pad at the end of each tar entry file content
     */
    protected void skipPad() throws IOException {
        if (bytesRead > 0) {
            int extra = (int) (bytesRead % TarConstants.DATA_BLOCK);

            if (extra > 0) {
                long bs = 0;
                while (bs < TarConstants.DATA_BLOCK - extra) {
                    long res = skip(TarConstants.DATA_BLOCK - extra - bs);
                    bs += res;
                }
            }
        }
    }

    /**
     * Skips 'n' bytes on the InputStream<br>
     * Overrides default implementation of skip
     * 
     */
    @Override
    public long skip(long n) throws IOException {
        if (defaultSkip) {
            // use skip method of parent stream
            // may not work if skip not implemented by parent
            long bs = super.skip(n);
            bytesRead += bs;

            return bs;
        }

        if (n <= 0) {
            return 0;
        }

        long left = n;
        byte[] sBuff = new byte[SKIP_BUFFER_SIZE];

        while (left > 0) {
            int res = read(sBuff, 0, (int) (left < SKIP_BUFFER_SIZE ? left : SKIP_BUFFER_SIZE));
            if (res < 0) {
                break;
            }
            left -= res;
        }

        return n - left;
    }

    public boolean isDefaultSkip() { return defaultSkip; }

    public void setDefaultSkip(boolean defaultSkip) { this.defaultSkip = defaultSkip; }
}

public static class TarOutputStream extends OutputStream {
    private final OutputStream out;
    private long bytesWritten;
    private long currentFileSize;
    private TarEntry currentEntry;

    public TarOutputStream(OutputStream out) {
        this.out = out;
        bytesWritten = 0;
        currentFileSize = 0;
    }

    public TarOutputStream(final File fout) throws FileNotFoundException {
        this.out = new BufferedOutputStream(new FileOutputStream(fout));
        bytesWritten = 0;
        currentFileSize = 0;
    }

    /**
     * Opens a file for writing. 
     */
    public TarOutputStream(final File fout, final boolean append) throws IOException {
        @SuppressWarnings("resource")
        RandomAccessFile raf = new RandomAccessFile(fout, "rw");
        final long fileSize = fout.length();
        if (append && fileSize > TarConstants.EOF_BLOCK) {
            raf.seek(fileSize - TarConstants.EOF_BLOCK);
        }
        out = new BufferedOutputStream(new FileOutputStream(raf.getFD()));
    }

    /**
     * Appends the EOF record and closes the stream
     * 
     * @see java.io.FilterOutputStream#close()
     */
    @Override
    public void close() throws IOException {
        closeCurrentEntry();
        write( new byte[TarConstants.EOF_BLOCK] );
        out.close();
    }
    /**
     * Writes a byte to the stream and updates byte counters
     * 
     * @see java.io.FilterOutputStream#write(int)
     */
    @Override
    public void write(int b) throws IOException {
        out.write( b );
        bytesWritten += 1;

        if (currentEntry != null) {
            currentFileSize += 1;
        }
    }

    /**
     * Checks if the bytes being written exceed the current entry size.
     * 
     * @see java.io.FilterOutputStream#write(byte[], int, int)
     */
    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        if (currentEntry != null && !currentEntry.isDirectory()) {
            if (currentEntry.getSize() < currentFileSize + len) {
                throw new IOException( "The current entry[" + currentEntry.getName() + "] size["
                        + currentEntry.getSize() + "] is smaller than the bytes[" + ( currentFileSize + len )
                        + "] being written." );
            }
        }

        out.write( b, off, len );
        
        bytesWritten += len;

        if (currentEntry != null) {
            currentFileSize += len;
        }        
    }

    /**
     * Writes the next tar entry header on the stream
     * 
     * @param entry
     * @throws IOException
     */
    public void putNextEntry(TarEntry entry) throws IOException {
        closeCurrentEntry();

        byte[] header = new byte[TarConstants.HEADER_BLOCK];
        entry.writeEntryHeader( header );

        write( header );

        currentEntry = entry;
    }

    /**
     * Closes the current tar entry
     * 
     * @throws IOException
     */
    protected void closeCurrentEntry() throws IOException {
        if (currentEntry != null) {
            if (currentEntry.getSize() > currentFileSize) {
                throw new IOException( "The current entry[" + currentEntry.getName() + "] of size["
                        + currentEntry.getSize() + "] has not been fully written." );
            }

            currentEntry = null;
            currentFileSize = 0;

            pad();
        }
    }

    /**
     * Pads the last content block
     * 
     * @throws IOException
     */
    protected void pad() throws IOException {
        if (bytesWritten > 0) {
            int extra = (int) ( bytesWritten % TarConstants.DATA_BLOCK );

            if (extra > 0) {
                write( new byte[TarConstants.DATA_BLOCK - extra] );
            }
        }
    }
}

public static class Octal {

    /**
     * Parse an octal string from a header buffer. This is used for the file permission mode value.
     */
    public static long parseOctal(byte[] header, int offset, int length) {
        long result = 0;
        boolean stillPadding = true;

        int end = offset + length;
        for (int i = offset; i < end; ++i) {
            if (header[i] == 0)
                break;

            if (header[i] == (byte) ' ' || header[i] == '0') {
                if (stillPadding)
                    continue;

                if (header[i] == (byte) ' ')
                    break;
            }

            stillPadding = false;

            result = ( result << 3 ) + ( header[i] - '0' );
        }

        return result;
    }

    /**
     * Parse an octal integer from a header buffer.
     */
    public static int getOctalBytes(long value, byte[] buf, int offset, int length) {
        int idx = length - 1;

        buf[offset + idx] = 0;
        --idx;
        buf[offset + idx] = (byte) ' ';
        --idx;

        if (value == 0) {
            buf[offset + idx] = (byte) '0';
            --idx;
        } else {
            for (long val = value; idx >= 0 && val > 0; --idx) {
                buf[offset + idx] = (byte) ( (byte) '0' + (byte) ( val & 7 ) );
                val = val >> 3;
            }
        }

        for (; idx >= 0; --idx) {
            buf[offset + idx] = (byte) ' ';
        }

        return offset + length;
    }

    /**
     * Parse the checksum octal integer from a header buffer.
     */
    public static int getCheckSumOctalBytes(long value, byte[] buf, int offset, int length) {
        getOctalBytes( value, buf, offset, length );
        buf[offset + length - 1] = (byte) ' ';
        buf[offset + length - 2] = 0;
        return offset + length;
    }

    /**
     * Parse an octal long integer from a header buffer.
     */
    public static int getLongOctalBytes(long value, byte[] buf, int offset, int length) {
        byte[] temp = new byte[length + 1];
        getOctalBytes( value, temp, 0, length + 1 );
        System.arraycopy( temp, 0, buf, offset, length );
        return offset + length;
    }
}

public static class TarUtils {

    /**
     * Determines the tar file size of the given folder/file path
     */
    public static long calculateTarSize(File path) { return tarSize(path) + TarConstants.EOF_BLOCK; }

    private static long tarSize(File dir) {
        long size = 0;

        if (dir.isFile()) {
            return entrySize(dir.length());
        } else {
            File[] subFiles = dir.listFiles();

            if (subFiles != null && subFiles.length > 0) {
                for (File file : subFiles) {
                    if (file.isFile()) {
                        size += entrySize(file.length());
                    } else {
                        size += tarSize(file);
                    }
                }
            } else {
                // Empty folder header
                return TarConstants.HEADER_BLOCK;
            }
        }

        return size;
    }

    private static long entrySize(long fileSize) {
        long size = 0;
        size += TarConstants.HEADER_BLOCK; // Header
        size += fileSize; // File size

        long extra = size % TarConstants.DATA_BLOCK;

        if (extra > 0) {
            size += (TarConstants.DATA_BLOCK - extra); // pad
        }

        return size;
    }

    public static String trim(String s, char c) {
        StringBuffer tmp = new StringBuffer(s);
        for (int i = 0; i < tmp.length(); i++) {
            if (tmp.charAt(i) != c) {
                break;
            } else {
                tmp.deleteCharAt(i);
            }
        }

        for (int i = tmp.length() - 1; i >= 0; i--) {
            if (tmp.charAt(i) != c) {
                break;
            } else {
                tmp.deleteCharAt(i);
            }
        }

        return tmp.toString();
    }
}

public static class PermissionUtils {

    /**
     * XXX: When using standard Java permissions, we treat 'owner' and 'group' equally and give no
     *      permissions for 'others'.
     */
    private static enum StandardFilePermission {
        EXECUTE(0110), WRITE(0220), READ(0440);

        private int mode;

        private StandardFilePermission(int mode) {
            this.mode = mode;
        }
    }

    private static Map<PosixFilePermission, Integer> posixPermissionToInteger = new HashMap<>();

    static {
        posixPermissionToInteger.put(PosixFilePermission.OWNER_EXECUTE, 0100);
        posixPermissionToInteger.put(PosixFilePermission.OWNER_WRITE, 0200);
        posixPermissionToInteger.put(PosixFilePermission.OWNER_READ, 0400);

        posixPermissionToInteger.put(PosixFilePermission.GROUP_EXECUTE, 0010);
        posixPermissionToInteger.put(PosixFilePermission.GROUP_WRITE, 0020);
        posixPermissionToInteger.put(PosixFilePermission.GROUP_READ, 0040);

        posixPermissionToInteger.put(PosixFilePermission.OTHERS_EXECUTE, 0001);
        posixPermissionToInteger.put(PosixFilePermission.OTHERS_WRITE, 0002);
        posixPermissionToInteger.put(PosixFilePermission.OTHERS_READ, 0004);
    }

    /**
     * Get file permissions in octal mode, e.g. 0755.
     */
    public static int permissions(File f) {
        if(f == null) {
            throw new NullPointerException("File is null.");
        }
        if(!f.exists()) {
            throw new IllegalArgumentException("File " + f + " does not exist.");
        }

        return isPosix ? posixPermissions(f) : standardPermissions(f);
    }

    private static final boolean isPosix = FileSystems.getDefault().supportedFileAttributeViews().contains("posix");

    private static int posixPermissions(File f) {
        int number = 0;
        try {
            Set<PosixFilePermission> permissions = Files.getPosixFilePermissions(f.toPath());
            for (Map.Entry<PosixFilePermission, Integer> entry : posixPermissionToInteger.entrySet()) {
                if (permissions.contains(entry.getKey())) {
                    number += entry.getValue();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return number;
    }

    private static Set<StandardFilePermission> readStandardPermissions(File f) {
        Set<StandardFilePermission> permissions = new HashSet<>();
        if(f.canExecute()) {
            permissions.add(StandardFilePermission.EXECUTE);
        }
        if(f.canWrite()) {
            permissions.add(StandardFilePermission.WRITE);
        }
        if(f.canRead()) {
            permissions.add(StandardFilePermission.READ);
        }
        return permissions;
    }

    private static Integer standardPermissions(File f) {
        int number = 0;
        Set<StandardFilePermission> permissions = readStandardPermissions(f);
        for (StandardFilePermission permission : permissions) {
            number += permission.mode;
        }
        return number;
    }
}

}