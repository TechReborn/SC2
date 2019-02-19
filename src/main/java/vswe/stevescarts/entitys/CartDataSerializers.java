package vswe.stevescarts.entitys;

import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializer;
import net.minecraft.network.datasync.DataSerializers;

import java.util.Arrays;

public class CartDataSerializers {

	public static void init() {
		DataSerializers.registerSerializer(VARINT);
	}

	public static final DataSerializer<int[]> VARINT = new DataSerializer<int[]>() {
		@Override
		public void write(PacketBuffer buf, int[] value) {
			buf.writeVarIntArray(value);
		}

		@Override
		public int[] read(PacketBuffer buf) {
			return buf.readVarIntArray();
		}

		@Override
		public DataParameter<int[]> createKey(int id) {
			return new DataParameter<>(id, this);
		}

		@Override
		public int[] copyValue(int[] p_192717_1_) {
			return p_192717_1_.clone();
		}
	};
}
