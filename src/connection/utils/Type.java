package connection.utils;

import connection.messages.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public enum Type implements Serializable {
    AUTH_REQUEST {
        @Override
        public AuthRequest deserializeMessage(DataInputStream in) throws IOException {
            return AuthRequest.deserialize(in);
        }
    },
    AUTH_REPLY {
        @Override
        public AuthReply deserializeMessage(DataInputStream in) throws IOException {
            return AuthReply.deserialize(in);
        }
    },
    JOB_REQUEST {
        @Override
        public JobRequest deserializeMessage(DataInputStream in) throws IOException {
            return JobRequest.deserialize(in);
        }
    },
    JOB_REPLY_OK {
        @Override
        public JobReplyOk deserializeMessage(DataInputStream in) throws IOException {
            return JobReplyOk.deserialize(in);
        }
    },
    JOB_REPLY_ERROR {
        @Override
        public JobReplyError deserializeMessage(DataInputStream in) throws IOException {
            return JobReplyError.deserialize(in);
        }
    },
    STATUS_REQUEST {
        @Override
        public StatusRequest deserializeMessage(DataInputStream in) {
            return new StatusRequest();
        }
    },
    STATUS_REPLY {
        @Override
        public StatusReply deserializeMessage(DataInputStream in) throws IOException {
            return StatusReply.deserialize(in);
        }
    };

    public void serialize(DataOutputStream out) throws IOException {
        out.writeChar(ordinal());
    }

    public static Type deserialize(DataInputStream in) throws IOException {
        return Type.values()[in.readChar()];
    }

    public abstract Message deserializeMessage(DataInputStream in) throws IOException;
}
