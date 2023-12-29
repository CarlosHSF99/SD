package connection.utils;

import connection.messages.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public enum Type implements Serializable {
    AUTH_REQUEST {
        @Override
        public AuthRequest deserializePayload(DataInputStream in) throws IOException {
            return AuthRequest.deserialize(in);
        }
    },
    AUTH_REPLY {
        @Override
        public AuthReply deserializePayload(DataInputStream in) throws IOException {
            return AuthReply.deserialize(in);
        }
    },
    JOB_REQUEST {
        @Override
        public JobRequest deserializePayload(DataInputStream in) throws IOException {
            return JobRequest.deserialize(in);
        }
    },
    JOB_REPLY_OK {
        @Override
        public JobReplyOk deserializePayload(DataInputStream in) throws IOException {
            return JobReplyOk.deserialize(in);
        }
    },
    JOB_REPLY_ERROR {
        @Override
        public JobReplyError deserializePayload(DataInputStream in) throws IOException {
            return JobReplyError.deserialize(in);
        }
    },
    STATUS_REQUEST {
        @Override
        public StatusRequest deserializePayload(DataInputStream in) {
            return new StatusRequest();
        }
    },
    STATUS_REPLY {
        @Override
        public StatusReply deserializePayload(DataInputStream in) throws IOException {
            return StatusReply.deserialize(in);
        }
    },
    WORKER_HANDSHAKE {
        @Override
        public WorkerHandshake deserializePayload(DataInputStream in) throws IOException {
            return WorkerHandshake.deserialize(in);
        }
    },
    USER_HANDSHAKE {
        @Override
        public UserHandshake deserializePayload(DataInputStream in) throws IOException {
            return UserHandshake.deserialize(in);
        }
    };

    public void serialize(DataOutputStream out) throws IOException {
        out.writeChar(ordinal());
    }

    public static Type deserialize(DataInputStream in) throws IOException {
        return Type.values()[in.readChar()];
    }

    public abstract Message deserializePayload(DataInputStream in) throws IOException;
}
