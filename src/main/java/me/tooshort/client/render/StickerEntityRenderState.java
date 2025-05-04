package me.tooshort.client.render;

import com.mojang.math.Axis;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.SignText;
import org.joml.Quaternionf;

public class StickerEntityRenderState extends EntityRenderState {
	public Direction faceDir = Direction.NORTH;
	public Direction horiDir = Direction.NORTH;

	public SignText text  = new SignText();
	public int      color = 0xFFFFFFFF;

	public Quaternionf rotation = calculateQuaternionRotation(faceDir, horiDir);

	public static Quaternionf calculateQuaternionRotation(Direction face, Direction hori) {
		if (face.getAxis().isHorizontal()) {
			float rot = face.toYRot();
			return Axis.YP.rotationDegrees(90+rot)
					.mul(Axis.ZP.rotationDegrees(90+2*rot))
					.mul(Axis.YP.rotationDegrees(180+2*rot));
		} else
			return Axis.ZP.rotationDegrees(180*(face.get3DDataValue()-1))
					.mul(Axis.YP.rotationDegrees(90-hori.toYRot()));
	}
}
