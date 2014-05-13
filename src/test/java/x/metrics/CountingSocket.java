package x.metrics;

import org.apache.commons.io.input.CountingInputStream;
import org.apache.commons.io.output.CountingOutputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

public class CountingSocket extends Socket {

    private final Socket realSocket;

    private CountingOutputStream outputStream;
    private CountingInputStream inputStream;

    public CountingSocket(Socket realSocket) {
        this.realSocket = realSocket;
    }

    @Override
    public void connect(SocketAddress endpoint) throws IOException {
        realSocket.connect(endpoint);
    }

    @Override
    public void connect(SocketAddress endpoint, int timeout) throws IOException {
        realSocket.connect(endpoint, timeout);
    }

    @Override
    public void bind(SocketAddress bindpoint) throws IOException {
        realSocket.bind(bindpoint);
    }

    @Override
    public InetAddress getInetAddress() {
        return realSocket.getInetAddress();
    }

    @Override
    public InetAddress getLocalAddress() {
        return realSocket.getLocalAddress();
    }

    @Override
    public int getPort() {
        return realSocket.getPort();
    }

    @Override
    public int getLocalPort() {
        return realSocket.getLocalPort();
    }

    @Override
    public SocketAddress getRemoteSocketAddress() {
        return realSocket.getRemoteSocketAddress();
    }

    @Override
    public SocketAddress getLocalSocketAddress() {
        return realSocket.getLocalSocketAddress();
    }

    @Override
    public SocketChannel getChannel() {
        return realSocket.getChannel();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        if (inputStream == null) {
            inputStream = new CountingInputStream(realSocket.getInputStream());
        }
        return inputStream;
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        if (outputStream == null) {
            outputStream = new CountingOutputStream(realSocket.getOutputStream());
        }
        return outputStream;
    }

    @Override
    public void setTcpNoDelay(boolean on) throws SocketException {
        realSocket.setTcpNoDelay(on);
    }

    @Override
    public boolean getTcpNoDelay() throws SocketException {
        return realSocket.getTcpNoDelay();
    }

    @Override
    public void setSoLinger(boolean on, int linger) throws SocketException {
        realSocket.setSoLinger(on, linger);
    }

    @Override
    public int getSoLinger() throws SocketException {
        return realSocket.getSoLinger();
    }

    @Override
    public void sendUrgentData(int data) throws IOException {
        realSocket.sendUrgentData(data);
    }

    @Override
    public void setOOBInline(boolean on) throws SocketException {
        realSocket.setOOBInline(on);
    }

    @Override
    public boolean getOOBInline() throws SocketException {
        return realSocket.getOOBInline();
    }

    @Override
    public void setSoTimeout(int timeout) throws SocketException {
        realSocket.setSoTimeout(timeout);
    }

    @Override
    public int getSoTimeout() throws SocketException {
        return realSocket.getSoTimeout();
    }

    @Override
    public void setSendBufferSize(int size) throws SocketException {
        realSocket.setSendBufferSize(size);
    }

    @Override
    public int getSendBufferSize() throws SocketException {
        return realSocket.getSendBufferSize();
    }

    @Override
    public void setReceiveBufferSize(int size) throws SocketException {
        realSocket.setReceiveBufferSize(size);
    }

    @Override
    public int getReceiveBufferSize() throws SocketException {
        return realSocket.getReceiveBufferSize();
    }

    @Override
    public void setKeepAlive(boolean on) throws SocketException {
        realSocket.setKeepAlive(on);
    }

    @Override
    public boolean getKeepAlive() throws SocketException {
        return realSocket.getKeepAlive();
    }

    @Override
    public void setTrafficClass(int tc) throws SocketException {
        realSocket.setTrafficClass(tc);
    }

    @Override
    public int getTrafficClass() throws SocketException {
        return realSocket.getTrafficClass();
    }

    @Override
    public void setReuseAddress(boolean on) throws SocketException {
        realSocket.setReuseAddress(on);
    }

    @Override
    public boolean getReuseAddress() throws SocketException {
        return realSocket.getReuseAddress();
    }

    @Override
    public void close() throws IOException {
        realSocket.close();
    }

    @Override
    public void shutdownInput() throws IOException {
        realSocket.shutdownInput();
    }

    @Override
    public void shutdownOutput() throws IOException {
        realSocket.shutdownOutput();
    }

    @Override
    public String toString() {
        return realSocket.toString();
    }

    @Override
    public boolean isConnected() {
        return realSocket.isConnected();
    }

    @Override
    public boolean isBound() {
        return realSocket.isBound();
    }

    @Override
    public boolean isClosed() {
        return realSocket.isClosed();
    }

    @Override
    public boolean isInputShutdown() {
        return realSocket.isInputShutdown();
    }

    @Override
    public boolean isOutputShutdown() {
        return realSocket.isOutputShutdown();
    }

    @Override
    public void setPerformancePreferences(int connectionTime, int latency, int bandwidth) {
        realSocket.setPerformancePreferences(connectionTime, latency, bandwidth);
    }

    public int getOutputByteCount() {
        return outputStream.getCount();
    }

    public int getInputByteCount() {
        return outputStream.getCount();
    }


}
