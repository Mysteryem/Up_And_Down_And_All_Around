package uk.co.mysterymayhem.gravitymod.asm;

import net.minecraftforge.fml.common.FMLLog;
import org.objectweb.asm.ClassReader;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
//import static uk.co.mysterymayhem.gravitymod.asm.ObfuscationHelper.DeobfAwareString;
//import static uk.co.mysterymayhem.gravitymod.asm.ObfuscationHelper.FieldInstruction;
//import static uk.co.mysterymayhem.gravitymod.asm.ObfuscationHelper.IDeobfAware;
//import static uk.co.mysterymayhem.gravitymod.asm.ObfuscationHelper.IDeobfAwareClass;
//import static uk.co.mysterymayhem.gravitymod.asm.ObfuscationHelper.PrimitiveClassName;
//import static uk.co.mysterymayhem.gravitymod.asm.ObfuscationHelper.ObjectClassName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.function.Function;

/**
 * Created by Mysteryem on 2016-08-16.
 */
public class Transformer implements IClassTransformer {

//    private static final IDeobfAware INIT = new DeobfAwareString("<init>");
//    private static final IDeobfAwareClass VOID = new PrimitiveClassName("V");
//    private static final IDeobfAwareClass CHAR = new PrimitiveClassName("C");
//    private static final IDeobfAwareClass DOUBLE = new PrimitiveClassName("D");
//    private static final IDeobfAwareClass FLOAT = new PrimitiveClassName("F");
//    private static final IDeobfAwareClass INT = new PrimitiveClassName("I");
//    private static final IDeobfAwareClass LONG = new PrimitiveClassName("J");
//    private static final IDeobfAwareClass SHORT = new PrimitiveClassName("S");
//    private static final IDeobfAwareClass BOOLEAN = new PrimitiveClassName("Z");

    private static final HashMap<String, Function<byte[], byte[]>> classNameToMethodMap = new HashMap<>();
    private static final String classToReplace = "net/minecraft/entity/player/EntityPlayer";
    private static final String classReplacement = "uk/co/mysterymayhem/gravitymod/asm/EntityPlayerWithGravity";

    static {
        //classNameToMethodMap.put("net.minecraft.entity.player.EntityPlayer", Transformer::patchEntityPlayerClass);
        //classNameToMethodMap.put("net.minecraft.entity.player.EntityPlayerSP", Transformer::patchEntityPlayerSPClass);
        classNameToMethodMap.put("net.minecraft.entity.player.EntityPlayerMP", Transformer::patchEntityPlayerSubClass);
        classNameToMethodMap.put("net.minecraft.client.entity.AbstractClientPlayer", Transformer::patchEntityPlayerSubClass);
        classNameToMethodMap.put("net.minecraft.client.entity.EntityPlayerSP", Transformer::patchEntityPlayerSP);
        classNameToMethodMap.put("net.minecraft.network.NetHandlerPlayServer", Transformer::patchNetHandlerPlayServer);
        classNameToMethodMap.put("net.minecraft.entity.Entity", Transformer::patchEntity);
        classNameToMethodMap.put("net.minecraft.entity.EntityLivingBase", Transformer::patchEntityLivingBase);
        classNameToMethodMap.put("net.minecraft.client.renderer.EntityRenderer", Transformer::patchEntityRenderer);
    }


    /*Entity.moveRelative

    Does water and flight movement apparently, not as important to get this working

    Could add a method override in EntityPlayer for EntityPlayer.moveRelative instead that calls super.moveRelative (less conflicts!)
     */

    /*Entity.moveEntity

    Take the input x, y and z motion and adjust ('rotate') it for the gravity direction of the entity, then run the rest of the method unchanged

    Could add a method override in EntityPlayer for EntityPlayer.moveEntity instead that calls super.moveEntity (less conflicts!)

    This could be a good place to set entity.onGround = true/false; dependent on the gravity direction
     */

    /*EntityPlayer.getLook(float partialTicks)

    Need to rotate the returned Vec3d to take into account how we've rotated the player's camera according to their gravity
     */

    /*EntityPlayerSP.func_189810_i
     */


    @Override
    public byte[] transform(String className, String transformedClassName, byte[] bytes) {
        if (bytes == null) {
            return null;
        }

        Function<byte[], byte[]> function = classNameToMethodMap.get(transformedClassName);

        if (function == null) {
            return bytes;
        } else {
            System.out.println("[UpAndDownAndAllAround] Patching " + className);
            return function.apply(bytes);
        }
    }

    private static byte[] patchEntityPlayerSubClass(byte[] bytes) {
        ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(bytes);
        classReader.accept(classNode, 0);

        classNode.superName = classReplacement;

        for (MethodNode methodNode : classNode.methods) {

            for (ListIterator<AbstractInsnNode> iterator = methodNode.instructions.iterator(); iterator.hasNext(); ) {
                AbstractInsnNode next = iterator.next();
                switch (next.getType()) {
                    case (AbstractInsnNode.TYPE_INSN):
                        TypeInsnNode typeInsnNode = (TypeInsnNode) next;
                        if (typeInsnNode.desc.equals(classToReplace)) {
                            typeInsnNode.desc = classReplacement;
                        }
                        break;
                    case (AbstractInsnNode.FIELD_INSN):
                        FieldInsnNode fieldInsnNode = (FieldInsnNode) next;
                        if (fieldInsnNode.owner.equals(classToReplace)) {
                            fieldInsnNode.owner = classReplacement;
                        }
                        break;
                    case (AbstractInsnNode.METHOD_INSN):
                        MethodInsnNode methodInsnNode = (MethodInsnNode) next;
                        if (methodInsnNode.owner.equals(classToReplace)) {
                            methodInsnNode.owner = classReplacement;
                        }
                        break;
                    default:
                        break;
                }
            }
//            while (iterator.hasNext()){
//
//                iterator.next();
//            }
        }

        System.out.println("[UpAndDownAndAllAround] Injected super class into " + classNode.name);

        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        classNode.accept(classWriter);
        return classWriter.toByteArray();
    }

    private static byte[] patchEntityRenderer(byte[] bytes) {
        ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(bytes);
        classReader.accept(classNode, 0);

        boolean WorldClient$rayTraceBlocks_found = false;

        for (MethodNode methodNode : classNode.methods) {
            if (methodNode.name.equals("orientCamera")) {
//                ArrayList<LocalVariableNode> localVarsCopy = new ArrayList<>(methodNode.localVariables);
//                localVarsCopy.sort((o1, o2) -> Integer.compare(o1.index, o2.index));
//                localVarsCopy.forEach(localVariableNode -> {
//                    FMLLog.info("name: " + localVariableNode.name + ", desc: " + localVariableNode.desc + ", index: " + localVariableNode.index);
//                });

                boolean foundEntityVar = false;
                int entityVar = -1;
                for (ListIterator<AbstractInsnNode> iterator = methodNode.instructions.iterator(); iterator.hasNext() && !WorldClient$rayTraceBlocks_found; ) {
                    AbstractInsnNode next = iterator.next();
                    if(!foundEntityVar && next instanceof VarInsnNode) {
                        VarInsnNode varInsnNode = (VarInsnNode)next;
                        if (varInsnNode.getOpcode() == Opcodes.ASTORE) {
                            entityVar = varInsnNode.var;
                            foundEntityVar = true;
                        }
                    }
                    else if (foundEntityVar && next instanceof MethodInsnNode) {
                        MethodInsnNode methodInsnNode = (MethodInsnNode)next;
                        if (methodInsnNode.getOpcode() == Opcodes.INVOKEVIRTUAL
                                && methodInsnNode.owner.equals("net/minecraft/client/multiplayer/WorldClient")
                                && methodInsnNode.name.equals("rayTraceBlocks")
                                && methodInsnNode.desc.equals("(Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Vec3d;)Lnet/minecraft/util/math/RayTraceResult;")) {
                            int index = 0;
                            //y, z, x
                            int[] localVariables = new int[3];
                            AbstractInsnNode nodeToInsertAfter = null;

                            while(index < 3) {
                                AbstractInsnNode previous = iterator.previous();
                                if (previous instanceof VarInsnNode) {
                                    VarInsnNode varInsnNode = (VarInsnNode)previous;
                                    if (varInsnNode.getOpcode() == Opcodes.DSTORE) {
                                        localVariables[index] = varInsnNode.var;
                                        if (index == 0) {
                                            nodeToInsertAfter = previous;
                                        }
                                        index++;
                                    }
                                }
                            }
                            while(!WorldClient$rayTraceBlocks_found) {
                                next = iterator.next();
                                if (next == nodeToInsertAfter) {
                                    iterator.add(new VarInsnNode(Opcodes.ALOAD, entityVar)); //this.entity
                                    iterator.add(new VarInsnNode(Opcodes.DLOAD, localVariables[2])); //x
                                    iterator.add(new VarInsnNode(Opcodes.DLOAD, localVariables[0])); //y
                                    iterator.add(new VarInsnNode(Opcodes.DLOAD, localVariables[1])); //z
                                    iterator.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                                            "uk/co/mysterymayhem/gravitymod/asm/Hooks",
                                            "adjustXYZ",
                                            "(Lnet/minecraft/entity/Entity;DDD)[D",
                                            false));

                                    iterator.add(new InsnNode(Opcodes.DUP));
                                    iterator.add(new InsnNode(Opcodes.DUP));

                                    iterator.add(new InsnNode(Opcodes.ICONST_0));
                                    iterator.add(new InsnNode(Opcodes.DALOAD));
                                    iterator.add(new VarInsnNode(Opcodes.DSTORE, localVariables[2])); //x = doubles[0]

                                    iterator.add(new InsnNode(Opcodes.ICONST_2));
                                    iterator.add(new InsnNode(Opcodes.DALOAD));
                                    iterator.add(new VarInsnNode(Opcodes.DSTORE, localVariables[1])); //z = doubles[0]

                                    iterator.add(new InsnNode(Opcodes.ICONST_1));
                                    iterator.add(new InsnNode(Opcodes.DALOAD));
                                    iterator.add(new VarInsnNode(Opcodes.DSTORE, localVariables[0])); //y = doubles[0]

                                    WorldClient$rayTraceBlocks_found = true;
                                }
                            }

//                            iterator.previous();// The INVOKEVIRTUAL we just found
//                            iterator.add(new VarInsnNode(Opcodes.ALOAD, 2));// Load the Entity
//                            iterator.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
//                                    "uk/co/mysterymayhem/gravitymod/asm/Hooks",
//                                    "adjustVec",
//                                    "(Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/entity/Entity;)Lnet/minecraft/util/math/Vec3d;",
//                                    false));
//                            WorldClient$rayTraceBlocks_found = true;
                        }
                    }
                }
            }
        }

        if (!WorldClient$rayTraceBlocks_found) {
            throw new RuntimeException("[UpAndDownAndAllAround] Could not find WorldClient::rayTraceBlocks in " + classNode.name + "::orientCamera");
        }

        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        classNode.accept(classWriter);
        return classWriter.toByteArray();
    }

    private static byte[] patchEntity(byte[] bytes) {
        ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(bytes);
        classReader.accept(classNode, 0);

        int numReplaced = 0;
        boolean newBlockPosFound = false;
        boolean blockPos$downFound = false;
        boolean Blocks$LADDERFound = false;

        for (MethodNode methodNode : classNode.methods) {
            if (methodNode.name.equals("moveEntity")) {
                for (ListIterator<AbstractInsnNode> iterator = methodNode.instructions.iterator(); iterator.hasNext(); ) {
                    AbstractInsnNode next = iterator.next();
                    if (next instanceof MethodInsnNode) {
                        MethodInsnNode methodInsnNode = (MethodInsnNode)next;
                        //TODO: Split into 3 if statements so it works with SRG names
                        if (methodInsnNode.getOpcode() == Opcodes.INVOKEVIRTUAL
                                && methodInsnNode.owner.equals("net/minecraft/util/math/AxisAlignedBB")
                                && methodInsnNode.name.startsWith("calculate")
                                && methodInsnNode.name.endsWith("Offset")
                                && methodInsnNode.desc.equals("(Lnet/minecraft/util/math/AxisAlignedBB;D)D")) {
                            //Simple 1:1 replacement
                            methodInsnNode.setOpcode(Opcodes.INVOKESTATIC);
                            methodInsnNode.owner = "uk/co/mysterymayhem/gravitymod/asm/Hooks";
                            //old name: calculate[XYZ]Offset
                            //new name: reverse[XYZ]Offset
                            methodInsnNode.name = methodInsnNode.name.replace("calculate", "reverse");
                            methodInsnNode.desc = "(Lnet/minecraft/util/math/AxisAlignedBB;Lnet/minecraft/util/math/AxisAlignedBB;D)D";
                            numReplaced++;
                        }
                        else if (!newBlockPosFound
                                && methodInsnNode.getOpcode() == Opcodes.INVOKESPECIAL
                                && methodInsnNode.owner.equals("net/minecraft/util/math/BlockPos")
                                && methodInsnNode.name.equals("<init>")
                                && methodInsnNode.desc.equals("(III)V")) {
                            // Starts with a simple 1:1 replacement
                            methodInsnNode.setOpcode(Opcodes.INVOKESTATIC);
                            methodInsnNode.owner = "uk/co/mysterymayhem/gravitymod/asm/Hooks";
                            methodInsnNode.name = "getImmutableBlockPosBelowEntity";
                            methodInsnNode.desc = "(Lnet/minecraft/entity/Entity;)Lnet/minecraft/util/math/BlockPos;";

                            iterator.previous(); // Our INNVOKESTATIC instruction

                            // Repeatedly remove the previous instruction until it's GETFIELD Entity.posX
                            // Remove that too
                            // If we were to call iterator.previous() again, we would get the ALOAD 0 that would normally
                            // be consumed by the GETFIELD instruction we just removed
                            // And it just so happens that our INVOKESTATIC instruction needs to have ALOAD 0 before it
                            // So we cut out a bunch of no longer used instructions and inserted our own instruction
                            // (hopefully it's fine to have local variables that are never used in bytecode)
                            for (;iterator.hasPrevious() && !newBlockPosFound;) {
                                AbstractInsnNode previous = iterator.previous();
                                if (previous instanceof FieldInsnNode) {
                                    FieldInsnNode fieldInsnNode = (FieldInsnNode)previous;
                                    if (fieldInsnNode.getOpcode() == Opcodes.GETFIELD
                                            && fieldInsnNode.owner.equals("net/minecraft/entity/Entity")
                                            && fieldInsnNode.name.equals("posX")
                                            && fieldInsnNode.desc.equals("D")) {
                                        newBlockPosFound = true;
                                    }
                                }
                                //TODO: Do the labels and line numbers need to be left in? Just the labels?
//                                if (previous instanceof LineNumberNode) {
//                                    continue;
//                                }
                                if (previous instanceof LabelNode) {
                                    continue;
                                }
                                iterator.remove();
                            }
                        }
                        else if (newBlockPosFound
                                && !blockPos$downFound) {
                            if (methodInsnNode.getOpcode() == Opcodes.INVOKEVIRTUAL
                                    && methodInsnNode.owner.equals("net/minecraft/util/math/BlockPos")
                                    && methodInsnNode.name.equals("down")
                                    && methodInsnNode.desc.equals("()Lnet/minecraft/util/math/BlockPos;")) {
                                methodInsnNode.setOpcode(Opcodes.INVOKESTATIC);
                                methodInsnNode.owner = "uk/co/mysterymayhem/gravitymod/asm/Hooks";
                                methodInsnNode.name = "getRelativeDownBlockPos";
                                methodInsnNode.desc = "(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/Entity;)Lnet/minecraft/util/math/BlockPos;";
                                iterator.previous();
                                VarInsnNode aload0 = new VarInsnNode(Opcodes.ALOAD, 0);
                                iterator.add(aload0);
                                blockPos$downFound = true;
                            }
                        }
                    }
                    else if (next instanceof FieldInsnNode
                            && newBlockPosFound && blockPos$downFound
                            /*&& !Blocks$LADDERFound*/) {
                        FieldInsnNode fieldInsnNode = (FieldInsnNode)next;
                        if (!Blocks$LADDERFound
                                && fieldInsnNode.getOpcode() == Opcodes.GETSTATIC
                                && fieldInsnNode.owner.equals("net/minecraft/init/Blocks")
                                && fieldInsnNode.name.equals("LADDER")
                                && fieldInsnNode.desc.equals("Lnet/minecraft/block/Block;")){
                            iterator.previous(); // returns the same GETSTATIC instruction
                            iterator.previous(); // should be ALOAD 29

                            // Start adding instructions just before the ALOAD 29 instruction
                            /*
                              Instruction // Top of stack
                                ALOAD 0   // this
                                DLOAD 30  // this,d12
                                DLOAD 32  // this,d12,d13
                                DLOAD 34  // this,d12,d13,d14
                                INVOKESTATIC uk/co/mysterymayhem/gravitymod/asm/Hooks.inverseAdjustXYZ (Lnet/minecraft/entity/Entity;DDD)[D
                                          // doubles
                                DUP       // doubles,doubles
                                DUP       // doubles,doubles,doubles
                                ICONST_0  // doubles,doubles,doubles,0
                                DALOAD    // doubles,doubles,doubles[0]
                                DSTORE 30 // doubles,doubles
                                ICONST_1  // doubles,doubles,1
                                DALOAD    // doubles,doubles[1]
                                DSTORE 32 // doubles
                                ICONST_2  // doubles,2
                                DALOAD    // doubles[2]
                                DSTORE 34 // [empty]
                            */
                            iterator.add(new VarInsnNode(Opcodes.ALOAD, 0));
                            iterator.add(new VarInsnNode(Opcodes.DLOAD, 73));//30
                            iterator.add(new VarInsnNode(Opcodes.DLOAD, 75));//32
                            iterator.add(new VarInsnNode(Opcodes.DLOAD, 77));//34
                            iterator.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                                    "uk/co/mysterymayhem/gravitymod/asm/Hooks",
                                    "inverseAdjustXYZ",
                                    "(Lnet/minecraft/entity/Entity;DDD)[D",
                                    false));
                            iterator.add(new InsnNode(Opcodes.DUP));
                            iterator.add(new InsnNode(Opcodes.DUP));
                            iterator.add(new InsnNode(Opcodes.ICONST_0));
                            iterator.add(new InsnNode(Opcodes.DALOAD));
                            iterator.add(new VarInsnNode(Opcodes.DSTORE, 73));//30
                            iterator.add(new InsnNode(Opcodes.ICONST_1));
                            iterator.add(new InsnNode(Opcodes.DALOAD));
                            iterator.add(new VarInsnNode(Opcodes.DSTORE, 75));//32
                            iterator.add(new InsnNode(Opcodes.ICONST_2));
                            iterator.add(new InsnNode(Opcodes.DALOAD));
                            iterator.add(new VarInsnNode(Opcodes.DSTORE, 77));//34
                            Blocks$LADDERFound = true;
                        }
                    }
                }
            }
        }

        if (numReplaced > 0) {
            System.out.println("[UpAndDownAndAllAround] Replaced " + numReplaced +
                    " usages of AxisAlignedBB:calculate[XYZ]Offset with Hooks::reverse[XYZ]Offset in " + classNode.name + "::moveEntity");
        }
        else {
            throw new RuntimeException("[UpAndDownAndAllAround] Could not find any AxisAlignedBB:calculate[XYZ]Offset methods in " + classNode.name + "::moveEntity");
        }
        if (newBlockPosFound) {

        }
        else {
            throw new RuntimeException("[UpAndDownAndAllAround] Failed to find new instance creation of BlockPos in Entity::moveEntity");
        }
        if (blockPos$downFound) {

        }
        else {
            throw new RuntimeException("[UpAndDownAndAllAround] Failed to find usage of BlockPos::down in Entity::moveEntity");
        }
        if (Blocks$LADDERFound) {

        }
        else {
            throw new RuntimeException("[UpAndDownAndAllAround] Failed to find usage of Blocks.LADDER in Entity::moveEntity");
        }

        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        classNode.accept(classWriter);
        return classWriter.toByteArray();
    }

    private static byte[] patchEntityLivingBase(byte[] bytes) {
        ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(bytes);
        classReader.accept(classNode, 0);

        boolean BlockPos$PooledMutableBlockPos$retainFound = false;
        boolean BlockPos$PooledMutableBlockPos$setPosFound = false;
        int numEntityLivingBase$isOffsetPositionInLiquidFound = 0;
        MethodInsnNode previousisOffsetPositionInLiquidMethodInsnNode = null;
        boolean GETFIELD_limbSwingAmount_found = false;
        boolean PUTFIELD_limbSwing_found = false;

        for (MethodNode methodNode : classNode.methods) {
            if (methodNode.name.equals("moveEntityWithHeading")) {
                for (ListIterator<AbstractInsnNode> iterator = methodNode.instructions.iterator(); iterator.hasNext(); ) {
                    AbstractInsnNode next = iterator.next();
                    if (next instanceof MethodInsnNode) {
                        MethodInsnNode methodInsnNode = (MethodInsnNode)next;
                        if (!BlockPos$PooledMutableBlockPos$retainFound
                                && methodInsnNode.getOpcode() == Opcodes.INVOKESTATIC
                                && methodInsnNode.owner.equals("net/minecraft/util/math/BlockPos$PooledMutableBlockPos")
                                && methodInsnNode.name.equals("retain")
                                && methodInsnNode.desc.equals("(DDD)Lnet/minecraft/util/math/BlockPos$PooledMutableBlockPos;")){
                            methodInsnNode.owner = "uk/co/mysterymayhem/gravitymod/asm/Hooks";
                            methodInsnNode.name = "getBlockPosBelowEntity";
                            methodInsnNode.desc = "(Lnet/minecraft/entity/Entity;)Lnet/minecraft/util/math/BlockPos$PooledMutableBlockPos;";

                            iterator.previous(); //Our replacment INVOKESTATIC

                            // Deletes everything before the INVOKESTATIC until we've reached our third ALOAD
                            int aload0foundCount = 0;
                            while(iterator.hasPrevious() && aload0foundCount < 3) {
                                AbstractInsnNode previous = iterator.previous();
                                if (previous instanceof VarInsnNode) {
                                    VarInsnNode varInsnNode = (VarInsnNode)previous;
                                    if (varInsnNode.getOpcode() == Opcodes.ALOAD
                                            && varInsnNode.var == 0) {
                                        aload0foundCount++;
                                    }
                                }
                                if (aload0foundCount < 3) {
                                    iterator.remove();
                                }
                            }
                            if (aload0foundCount == 3) {
                                BlockPos$PooledMutableBlockPos$retainFound = true;
                            }
                        }
                        else if (BlockPos$PooledMutableBlockPos$retainFound
                                && !BlockPos$PooledMutableBlockPos$setPosFound
                                && methodInsnNode.getOpcode() == Opcodes.INVOKEVIRTUAL
                                && methodInsnNode.owner.equals("net/minecraft/util/math/BlockPos$PooledMutableBlockPos")
                                && methodInsnNode.name.equals("setPos")
                                && methodInsnNode.desc.equals("(DDD)Lnet/minecraft/util/math/BlockPos$PooledMutableBlockPos;")) {
                            methodInsnNode.setOpcode(Opcodes.INVOKESTATIC);
                            methodInsnNode.owner = "uk/co/mysterymayhem/gravitymod/asm/Hooks";
                            methodInsnNode.name = "setPooledMutableBlockPosToBelowEntity";
                            methodInsnNode.desc = "(Lnet/minecraft/util/math/BlockPos$PooledMutableBlockPos;Lnet/minecraft/entity/Entity;)Lnet/minecraft/util/math/BlockPos$PooledMutableBlockPos;";

                            iterator.previous(); //Our replacement INVOKESTATIC

                            // Interestingly, we have to find the same number of ALOAD 0 instructions as the previous
                            // Deletes everything before the INVOKESTATIC until we've reached our third ALOAD
                            int aload0foundCount = 0;
                            while (iterator.hasPrevious() && aload0foundCount < 3) {
                                AbstractInsnNode previous = iterator.previous();
                                if (previous instanceof VarInsnNode) {
                                    VarInsnNode varInsnNode = (VarInsnNode)previous;
                                    if (varInsnNode.getOpcode() == Opcodes.ALOAD
                                            && varInsnNode.var == 0) {
                                        aload0foundCount++;
                                    }
                                }
                                if (aload0foundCount < 3) {
                                    iterator.remove();
                                }
                            }
                            if (aload0foundCount == 3) {
                                BlockPos$PooledMutableBlockPos$setPosFound = true;
                            }
                        }
                        else if (BlockPos$PooledMutableBlockPos$setPosFound
                                && numEntityLivingBase$isOffsetPositionInLiquidFound < 2
                                && methodInsnNode.getOpcode() == Opcodes.INVOKEVIRTUAL
                                && methodInsnNode.owner.equals("net/minecraft/entity/EntityLivingBase")
                                && methodInsnNode.name.equals("isOffsetPositionInLiquid")
                                && methodInsnNode.desc.equals("(DDD)Z")){
                            if (methodInsnNode == previousisOffsetPositionInLiquidMethodInsnNode) {
                                continue;
                            }
                            previousisOffsetPositionInLiquidMethodInsnNode = methodInsnNode;
                            int numReplacementsMade = 0;
                            while (iterator.hasPrevious() && numReplacementsMade < 2) {
                                AbstractInsnNode previous = iterator.previous();
                                if (previous instanceof FieldInsnNode) {
                                    FieldInsnNode fieldInsnNode = (FieldInsnNode)previous;
                                    if (fieldInsnNode.getOpcode() == Opcodes.GETFIELD
                                            && fieldInsnNode.owner.equals("net/minecraft/entity/EntityLivingBase")
                                            && fieldInsnNode.name.equals("posY")
                                            && fieldInsnNode.desc.equals("D")) {
                                        // Replace the GETFIELD with an INVOKESTATIC of the Hook
                                        iterator.remove();

                                        iterator.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                                                "uk/co/mysterymayhem/gravitymod/asm/Hooks",
                                                "getRelativePosY",
                                                "(Lnet/minecraft/entity/Entity;)D",
                                                false));
                                        numReplacementsMade++;
                                    }
                                }
                            }
                            if (numReplacementsMade == 2) {
                                numEntityLivingBase$isOffsetPositionInLiquidFound++;
                            }
                        }
                    }
                    else if(next instanceof FieldInsnNode) {
                        FieldInsnNode fieldInsnNode = (FieldInsnNode)next;
                        if (numEntityLivingBase$isOffsetPositionInLiquidFound == 2
                                && !GETFIELD_limbSwingAmount_found
                                && fieldInsnNode.getOpcode() == Opcodes.GETFIELD
                                && fieldInsnNode.owner.equals("net/minecraft/entity/EntityLivingBase")
                                && fieldInsnNode.name.equals("limbSwingAmount")
                                && fieldInsnNode.desc.equals("F")) {
                            iterator.previous(); // The GETFIELD instruction
                            iterator.previous(); // ALOAD 0
                            iterator.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                                    "uk/co/mysterymayhem/gravitymod/asm/Hooks",
                                    "makePositionRelative",
                                    "(Lnet/minecraft/entity/EntityLivingBase;)V",
                                    false));
                            iterator.add(new VarInsnNode(Opcodes.ALOAD, 0));
                            GETFIELD_limbSwingAmount_found = true;
                        }
                        else if (GETFIELD_limbSwingAmount_found
                                && !PUTFIELD_limbSwing_found
                                && fieldInsnNode.getOpcode() == Opcodes.PUTFIELD
                                && fieldInsnNode.owner.equals("net/minecraft/entity/EntityLivingBase")
                                && fieldInsnNode.name.equals("limbSwing")
                                && fieldInsnNode.desc.equals("F")) {
                            iterator.add(new VarInsnNode(Opcodes.ALOAD, 0));
                            iterator.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                                    "uk/co/mysterymayhem/gravitymod/asm/Hooks",
                                    "makePositionAbsolute",
                                    "(Lnet/minecraft/entity/EntityLivingBase;)V",
                                    false));
                            PUTFIELD_limbSwing_found = true;
                        }
                    }
                }
            }
        }

        if (BlockPos$PooledMutableBlockPos$retainFound) {

        }
        else {
            throw new RuntimeException("");
        }
        if (BlockPos$PooledMutableBlockPos$setPosFound) {

        }
        else {
            throw new RuntimeException("");
        }
        if (numEntityLivingBase$isOffsetPositionInLiquidFound == 2) {

        }
        else {
            throw new RuntimeException("");
        }
        if (GETFIELD_limbSwingAmount_found) {

        }
        else {
            throw new RuntimeException("[UpAndDownAndAllAround] Failed to find GETFIELD this.limbSwingAmount in EntityLivingBase::moveEntityWithHeading");
        }
        if (PUTFIELD_limbSwing_found) {

        }
        else {
            throw new RuntimeException("[UpAndDownAndAllAround] Failed to find PUTFIELD this.limbSwing in EntityLivingBase::moveEntityWithHeading");
        }

        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        classNode.accept(classWriter);
        return classWriter.toByteArray();
    }

    private static byte[] patchNetHandlerPlayServer(byte[] bytes) {
        ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(bytes);
        classReader.accept(classNode, 0);

        boolean foundMoveEntity = false;
        boolean foundHandleFalling = false;

        for (MethodNode methodNode : classNode.methods) {
            if (methodNode.name.equals("processPlayer")) {
                System.out.println("Modifying NetHandlerPlayServer::processPlayer");
                for (ListIterator<AbstractInsnNode> iterator = methodNode.instructions.iterator(); iterator.hasNext(); ) {
                    AbstractInsnNode next = iterator.next();
                    if (next instanceof MethodInsnNode) {
                        MethodInsnNode methodInsnNode = (MethodInsnNode) next;
                        if (!foundMoveEntity
                                && methodInsnNode.getOpcode() == Opcodes.INVOKEVIRTUAL
                                && methodInsnNode.owner.equals("net/minecraft/entity/player/EntityPlayerMP")
                                && methodInsnNode.name.equals("moveEntity")
                                && methodInsnNode.desc.equals("(DDD)V"))
                        {
                            System.out.println("Found \"playerEntity.moveEntity(...)\"");
                            methodInsnNode.setOpcode(Opcodes.INVOKESTATIC);
                            methodInsnNode.owner = "uk/co/mysterymayhem/gravitymod/asm/Hooks";
                            methodInsnNode.name = "moveEntityAbsolute";
                            methodInsnNode.desc = "(Lnet/minecraft/entity/player/EntityPlayer;DDD)V";
                            System.out.println("Replaced with \"Hooks.moveEntityAbsolute(...)\"");
                            foundMoveEntity = true;
                        }
                        else if (!foundHandleFalling
                                && methodInsnNode.getOpcode() == Opcodes.INVOKEVIRTUAL
                                && methodInsnNode.owner.equals("net/minecraft/entity/player/EntityPlayerMP")
                                && methodInsnNode.name.equals("handleFalling")
                                && methodInsnNode.desc.equals("(DZ)V"))
                        {
                            System.out.println("Found \"playerEntity.handleFalling(...)\"");
                            iterator.previous(); // INVOKEVIRTUAL handleFalling again
                            iterator.previous(); // INVOKEVIRTUAL isOnGround
                            iterator.previous(); // ALOAD 1 (CPacketPlayer method argument)
                            next = iterator.previous(); // Should be DSUB
                            if (next.getOpcode() == Opcodes.DSUB) {
                                iterator.remove();
                                VarInsnNode dload7 = new VarInsnNode(Opcodes.DLOAD, 7);
                                iterator.add(dload7);
                                MethodInsnNode invokeStaticHook = new MethodInsnNode(Opcodes.INVOKESTATIC,
                                        "uk/co/mysterymayhem/gravitymod/asm/Hooks",
                                        "netHandlerPlayServerHandleFallingYChange",
                                        "(Lnet/minecraft/entity/player/EntityPlayerMP;DDD)D",
                                        false);
                                iterator.add(invokeStaticHook);
                                iterator.previous(); // Our newly added INVOKESTATIC
                                iterator.previous(); // Our newly added DLOAD 7
                                iterator.previous(); // DLOAD 9
                                next = iterator.previous(); // GETFIELD posY
                                if (next instanceof FieldInsnNode) {
                                    FieldInsnNode fieldInsnNode = (FieldInsnNode)next;
                                    if (fieldInsnNode.owner.equals("net/minecraft/entity/player/EntityPlayerMP")
                                            && fieldInsnNode.name.equals("posY")
                                            && fieldInsnNode.desc.equals("D"))
                                    {
                                        VarInsnNode dload3 = new VarInsnNode(Opcodes.DLOAD, 3);
                                        iterator.remove();
                                        iterator.add(dload3);
                                        foundHandleFalling = true;
                                    }
                                    else {
                                        throw new RuntimeException("[UpAndDownAndAllAround] Unexpected instruction in NetHandletPlayServer::processPlayer, expecting \"GETFIELD posY\"");
                                    }
                                }
                                else {
                                    throw new RuntimeException("[UpAndDownAndAllAround] Unexpected instruction in NetHandletPlayServer::processPlayer, expecting \"GETFIELD posY\"");
                                }
                            }
                            else {
                                throw new RuntimeException("[UpAndDownAndAllAround] Unexpected instruction in NetHandletPlayServer::processPlayer, expecting \"DSUB\"");
                            }
                        }
                    }
                }
            }
        }

        if (foundMoveEntity) {
            System.out.println("[UpAndDownAndAllAround] Replaced \"playerEntity.moveEntity(...)\" with \"Hooks.moveEntityAbsolute(...)\" in " + classNode.name +"::processPlayer");
        } else {
            throw new RuntimeException("Could not find \"playerEntity.moveEntity(...)\" in " + classNode.name);
        }
        if (foundHandleFalling) {
            System.out.println("[UpAndDownAndAllAround] Replaced \"this.playerEntity.handleFalling(this.playerEntity.posY - d3, packetIn.isOnGround());\" with \"this.playerEntity.handleFalling(Hooks.netHandlerPlayServerHandleFallingYChange(playerEntity, d0, d3, d2), packetIn.isOnGround());\" in " + classNode.name + "::processPlayer");
        } else {
            throw new RuntimeException("Could not find \"this.playerEntity.handleFalling(this.playerEntity.posY - d3, packetIn.isOnGround());\" in " + classNode.name +"::processPlayer");
        }

        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        classNode.accept(classWriter);
        return classWriter.toByteArray();
    }

    //TODO: Instead of changing the field, call a method in Hooks that takes the aabb as an argument and returns GravityAxisAlignedBB.getOrigin().getY() or AxisAlignedBB.minY
    private static byte[] patchEntityPlayerSP(byte[] bytes) {
        ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(bytes);
        classReader.accept(classNode, 0);

        boolean isHeadSpaceFreeModified = false, onUpdateWalkingPlayerModified = false, onLivingUpdateModified = false;

        int numReplacements = 0;

//        IDeobfAware onUpdateWalkingPlayer = new DeobfAwareString("onUpdateWalkingPlayer", "func_175161_p");
//        IDeobfAwareClass AxisAlignedBB = new ObjectClassName("net/minecraft/util/math/AxisAlignedBB");
//        IDeobfAware minY = new DeobfAwareString("minY", "field_72338_b");
//        FieldInstruction minYFieldInsn = new FieldInstruction(Opcodes.GETFIELD, AxisAlignedBB, minY, DOUBLE);
//        IDeobfAwareClass EntityPlayerSP = new ObjectClassName("net/minecraft/client/entity/EntityPlayerSP");
//        IDeobfAware posY = new DeobfAwareString("posY", "field_70163_u");
//        FieldInstruction posYFieldInsn = new FieldInstruction(Opcodes.GETFIELD, EntityPlayerSP, posY, DOUBLE);

//        IDeobfAware onLivingUpdate = new DeobfAwareString("onLivingUpdate", "func_70636_d");

        for (MethodNode methodNode : classNode.methods) {
            if (methodNode.name.equals("onUpdateWalkingPlayer")) {
                System.out.println("[UpAndDownAndAllAround] Modifying EntityPlayerSP::onUpdateWalkingPlayer");

                for (ListIterator<AbstractInsnNode> iterator = methodNode.instructions.iterator(); iterator.hasNext(); ) {
                    AbstractInsnNode next = iterator.next();
                    if (next instanceof FieldInsnNode) {
                        FieldInsnNode fieldInsnNode = (FieldInsnNode) next;
                        if (fieldInsnNode.getOpcode() == Opcodes.GETFIELD) {
                            if (fieldInsnNode.owner.equals("net/minecraft/util/math/AxisAlignedBB")
                                    && fieldInsnNode.name.equals("minY")
                                    && fieldInsnNode.desc.equals("D")) {
                                if (iterator.hasPrevious()) {
                                    iterator.previous();
                                    AbstractInsnNode previous = iterator.previous();
                                    if (previous instanceof VarInsnNode) {
                                        VarInsnNode varInsnNode = (VarInsnNode) previous;
                                        if (varInsnNode.var == 3) {
                                                /*
                                                Replace
                                                axisalignedbb.minY
                                                with
                                                this.posY
                                                in
                                                EntityPlayerSP::onUpdateWalkingPlayer
                                                 */
                                            varInsnNode.var = 0;
                                            fieldInsnNode.owner = "net/minecraft/client/entity/EntityPlayerSP";
                                            fieldInsnNode.name = "posY";
                                            numReplacements++;

                                            // We went back two with .previous(), so go forwards again the same amount
                                            iterator.next();
                                            iterator.next();
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                if (numReplacements != 0) {
                    System.out.println("[UpAndDownAndAllAround] Replaced \"axisalignedbb.minY\" with \"this.posY\" " + numReplacements + " time" + (numReplacements == 1 ? "" : "s") + " in " + classNode.name);
                    onUpdateWalkingPlayerModified = true;
                } else {
                    throw new RuntimeException("[UpAndDownAndAllAround] Failed to find any instances of \"axisalignedbb.minY\" in " + classNode.name);
                }
            } else if (methodNode.name.equals("onLivingUpdate")) {
                System.out.println("[UpAndDownAndAllAround] Modifying EntityPlayerSP::onLivingUpdate");

                boolean error = true;
                for (ListIterator<AbstractInsnNode> iterator = methodNode.instructions.iterator(); iterator.hasNext(); ) {
                    AbstractInsnNode next = iterator.next();
                    if (next instanceof VarInsnNode) {
                        VarInsnNode varInsnNode = (VarInsnNode) next;
                        if (varInsnNode.var == 6 && varInsnNode.getOpcode() == Opcodes.ASTORE) {
                            System.out.println("[UpAndDownAndAllAround] Found \"ASTORE 6\"");

                            VarInsnNode aLoad6 = new VarInsnNode(Opcodes.ALOAD, 6);
                            TypeInsnNode instanceofGravityAxisAlignedBB = new TypeInsnNode(Opcodes.INSTANCEOF, "uk/co/mysterymayhem/gravitymod/util/GravityAxisAlignedBB");
                            JumpInsnNode ifeqJumpInsnNode = new JumpInsnNode(Opcodes.IFEQ, null);
                            VarInsnNode aLoad0 = new VarInsnNode(Opcodes.ALOAD, 0);
                            VarInsnNode aLoad6_2 = new VarInsnNode(Opcodes.ALOAD, 6);
                            MethodInsnNode callHook = new MethodInsnNode(Opcodes.INVOKESTATIC,
                                    "uk/co/mysterymayhem/gravitymod/asm/Hooks",
                                    "pushEntityPlayerSPOutOfBlocks",
                                    "(Luk/co/mysterymayhem/gravitymod/asm/EntityPlayerWithGravity;Lnet/minecraft/util/math/AxisAlignedBB;)V", false);
                            JumpInsnNode gotoJumpInsnNode = new JumpInsnNode(Opcodes.GOTO, null);

                            iterator.add(aLoad6);
                            iterator.add(instanceofGravityAxisAlignedBB);
                            iterator.add(ifeqJumpInsnNode);
                            iterator.add(aLoad0);
                            iterator.add(aLoad6_2);
                            iterator.add(callHook);
                            iterator.add(gotoJumpInsnNode);

                            next = iterator.next();

                            if (next instanceof LabelNode) {
                                ifeqJumpInsnNode.label = (LabelNode) next;

                                next = iterator.next();

                                if (next instanceof LineNumberNode) {
                                    // ClassWriter is going to compute the frames, the commented out code is untested
//                                    FrameNode frameAppendNode = new FrameNode(Opcodes.F_APPEND, 1, new Object[]{"net/minecraft/util/math/AxisAlignedBB"}, 0, new Object[0]);
//                                    iterator.add(frameAppendNode);

                                    LabelNode labelForGotoJumpInsnNode = null;
                                    for (; iterator.hasNext(); ) {
                                        next = iterator.next();
                                        if (next instanceof MethodInsnNode) {
                                            MethodInsnNode methodInsnNode = (MethodInsnNode) next;
                                            if (methodInsnNode.getOpcode() == Opcodes.INVOKEVIRTUAL
                                                    && methodInsnNode.owner.equals("net/minecraft/client/entity/EntityPlayerSP")
                                                    && methodInsnNode.name.equals("getFoodStats")
                                                    && methodInsnNode.desc.equals("()Lnet/minecraft/util/FoodStats;")) {
                                                System.out.println("[UpAndDownAndAllAround] Found \"this.getFoodStats\"");
                                                for (; iterator.hasPrevious(); ) {
                                                    AbstractInsnNode previous = iterator.previous();
                                                    if (previous instanceof LabelNode) {
                                                        System.out.println("[UpAndDownAndAllAround] Found previous Label");
                                                        labelForGotoJumpInsnNode = (LabelNode) previous;
                                                        // ClassWriter is going to compute the frames, the commented out code is untested
//                                                        for(;iterator.hasNext();) {
//                                                            next = iterator.next();
//                                                            if (next instanceof VarInsnNode) {
//                                                                varInsnNode = (VarInsnNode)next;
//                                                                if (varInsnNode.getOpcode() == Opcodes.ALOAD && varInsnNode.var == 0) {
//                                                                    FrameNode frameSameNode = new FrameNode(Opcodes.F_SAME, 0, new Object[0], 0, new Object[0]);
//                                                                    break;
//                                                                }
//                                                            }
//                                                        }
                                                        break;
                                                    }
                                                }
                                                break;
                                            }
                                        }
                                    }
                                    if (labelForGotoJumpInsnNode != null) {
                                        gotoJumpInsnNode.label = labelForGotoJumpInsnNode;
                                        error = false;
                                    }
                                    break;
                                }
                            }
                            break;
                        }
                    }
                }
                if (error) {
                    throw new RuntimeException("[UpAndDownAndAllAround] Hook insertion into EntityPlayerSP::onLivingUpdate failed");
                } else {
                    System.out.println("[UpAndDownAndAllAround] Inserted an if statement and call to \"Hooks.pushEntityPlayerSPOutOfBlocks\" into \"EntityPlayerSP::onLivingUpdate\"");
                    onLivingUpdateModified = true;
                }
            }
            else if (methodNode.name.equals("isHeadspaceFree")) {
                System.out.println("[UpAndDownAndAllAround] Modifying EntityPlayerSP::isHeadspaceFree");
                InsnList instructions = methodNode.instructions;
                AbstractInsnNode label = instructions.get(0);
                AbstractInsnNode lineNumber = instructions.get(1);
                AbstractInsnNode labelEnd = instructions.getLast();
                AbstractInsnNode returnNode = labelEnd.getPrevious();

                instructions.clear();
                instructions.add(label);
                instructions.add(lineNumber);
                instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
                instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
                instructions.add(new VarInsnNode(Opcodes.ILOAD, 2));
                instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                        "uk/co/mysterymayhem/gravitymod/asm/Hooks",
                        "isHeadspaceFree",
                        "(Lnet/minecraft/client/entity/EntityPlayerSP;Lnet/minecraft/util/math/BlockPos;I)Z",
                        false));
                instructions.add(new InsnNode(Opcodes.IRETURN));
                instructions.add(labelEnd);
                System.out.println("[UpAndDownAndAllAround] Replaced EntityPlayerSP::isHeadspaceFree with call to Hooks::isHeadspaceFree");
                isHeadSpaceFreeModified = true;
            }
        }

        if (isHeadSpaceFreeModified && onLivingUpdateModified && onUpdateWalkingPlayerModified) {
            ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
            classNode.accept(classWriter);
            return classWriter.toByteArray();
        }
        else {
            throw new RuntimeException("[UpAndDownAndAllAround] Modification of EntityPlayerSP failed, UpAndDownAndAllAround is probably being used with the wrong Minecraft version.");
        }

    }

//    private static byte[] patchEntityPlayerClass(byte[] bytes) {
//        String getLookMethodName = FMLLoadingPlugin.runtimeDeobfEnabled ? "getVectorForRotation" : "func_174806_f";
//
//        ClassNode classNode = new ClassNode();
//        ClassReader classReader = new ClassReader(bytes);
//        classReader.accept(classNode, 0);
//
//        Iterator<MethodNode> methods = classNode.methods.iterator();
//        while (methods.hasNext()) {
//            MethodNode node = methods.next();
//
//            if (node.)
//        }
//
//        return bytes;
//    }
//
//    private static byte[] patchEntityPlayerSPClass(byte[] bytes) {
//        return bytes;
//    }
}
